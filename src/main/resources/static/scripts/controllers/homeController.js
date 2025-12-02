blogApp.controller('HomeController', ['$scope', '$http', '$interval', function($scope, $http, $interval) {
    $scope.message = 'ブログシステムへようこそ';
    $scope.definition = null;
    $scope.sectionData = {}; // 各セクションのデータを格納
    $scope.loading = true;
    $scope.error = null;
    $scope.heroSlides = {}; // スライドショーの現在のインデックス
    
    // ホーム画面の定義を読み込む
    $http.get('/api/screen/home/definition').then(function(defResp) {
        console.log('Home definition loaded:', defResp.data);
        $scope.definition = defResp.data;
        
        // 各セクションのデータを読み込む
        var promises = [];
        
        $scope.definition.sections.forEach(function(section, index) {
            console.log('Processing section ' + index + ':', section.type);
            switch(section.type) {
                case 'recent-posts':
                    promises.push(loadRecentPosts(section, index));
                    break;
                case 'category-grid':
                    promises.push(loadCategoryGrid(section, index));
                    break;
                case 'stats':
                    promises.push(loadStats(section, index));
                    break;
                case 'hero':
                    initHeroSlideshow(section, index);
                    break;
            }
        });
        
        return Promise.all(promises);
    }).catch(function(err) {
        console.error('Failed to load home definition:', err);
        console.error('Error details:', err.data);
        console.error('Error status:', err.status);
        console.error('Error message:', err.statusText);
        $scope.error = 'ホーム画面の読み込みに失敗しました: ' + (err.statusText || 'Unknown error');
    }).finally(function() {
        $scope.loading = false;
    });
    
    // 最新記事を読み込む
    function loadRecentPosts(section, index) {
        var params = {
            page: 0,
            pageSize: section.limit || 5
        };
        
        if (section.whereClause) {
            params.status = 'published';
        }
        
        console.log('Loading recent posts with params:', params);
        
        return $http.get('/api/screen/' + section.sourceTable.replace('blog_', '') + '/data', { params: params })
            .then(function(resp) {
                console.log('Recent posts loaded:', resp.data);
                $scope.sectionData[index] = resp.data.records || [];
            })
            .catch(function(err) {
                console.error('Failed to load recent posts:', err);
                $scope.sectionData[index] = [];
            });
    }
    
    // カテゴリグリッドを読み込む
    function loadCategoryGrid(section, index) {
        console.log('Loading category grid');
        return $http.get('/api/screen/' + section.sourceTable.replace('blog_', '') + '/data', { 
            params: { page: 0, pageSize: 100 } 
        }).then(function(resp) {
            console.log('Category grid loaded:', resp.data);
            $scope.sectionData[index] = resp.data.records || [];
        })
        .catch(function(err) {
            console.error('Failed to load category grid:', err);
            $scope.sectionData[index] = [];
        });
    }
    
    // 統計情報を読み込む
    function loadStats(section, index) {
        console.log('Loading stats for section:', section);
        var promises = section.items.map(function(item) {
            return $http.get('/api/screen/' + item.sourceTable.replace('blog_', '') + '/data', {
                params: { page: 0, pageSize: 1 }
            }).then(function(resp) {
                return resp.data.total || 0;
            })
            .catch(function(err) {
                console.error('Failed to load stat for ' + item.sourceTable + ':', err);
                return 0;
            });
        });
        
        return Promise.all(promises).then(function(counts) {
            console.log('Stats loaded:', counts);
            $scope.sectionData[index] = counts;
        });
    }
    
    // ヒーロースライドショーを初期化
    function initHeroSlideshow(section, index) {
        if (!section.slides || section.slides.length === 0) return;
        
        $scope.heroSlides[index] = {
            current: 0,
            total: section.slides.length
        };
        
        if (section.autoplay && section.slides.length > 1) {
            var interval = section.interval || 5000;
            $interval(function() {
                $scope.nextSlide(index, section.slides.length);
            }, interval);
        }
    }
    
    // 次のスライドへ
    $scope.nextSlide = function(sectionIndex, totalSlides) {
        if (!$scope.heroSlides[sectionIndex]) return;
        $scope.heroSlides[sectionIndex].current = 
            ($scope.heroSlides[sectionIndex].current + 1) % totalSlides;
    };
    
    // 前のスライドへ
    $scope.prevSlide = function(sectionIndex, totalSlides) {
        if (!$scope.heroSlides[sectionIndex]) return;
        $scope.heroSlides[sectionIndex].current = 
            ($scope.heroSlides[sectionIndex].current - 1 + totalSlides) % totalSlides;
    };
    
    // 特定のスライドへ
    $scope.goToSlide = function(sectionIndex, slideIndex) {
        if (!$scope.heroSlides[sectionIndex]) return;
        $scope.heroSlides[sectionIndex].current = slideIndex;
    };
    
    // 記事詳細へのリンクを生成
    $scope.getPostLink = function(post) {
        return '#!/screen/blog/detail/' + post.id;
    };
    
    // リンクパターンを適用
    $scope.applyLinkPattern = function(pattern, item) {
        return pattern.replace(/\{\{(\w+)\}\}/g, function(match, key) {
            return item[key] || '';
        });
    };
}]);


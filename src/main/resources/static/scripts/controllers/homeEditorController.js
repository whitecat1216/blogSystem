blogApp.controller('HomeEditorController', ['$scope', '$http', '$window', function($scope, $http, $window) {
    $scope.config = null;
    $scope.sections = [];
    $scope.loading = true;
    $scope.saving = false;
    $scope.error = null;
    $scope.success = null;
    $scope.editMode = 'visual'; // 'visual' or 'json'
    $scope.jsonText = '';
    $scope.history = [];
    $scope.showHistory = false;
    $scope.showPreview = false;
    
    // セクションタイプのテンプレート
    $scope.sectionTemplates = {
        'hero': {
            type: 'hero',
            style: 'slideshow',
            slides: [{
                image: '',
                title: '',
                subtitle: '',
                buttonText: '',
                buttonLink: '',
                overlay: { enabled: true, color: '#000000', opacity: 0.4 }
            }],
            autoplay: true,
            interval: 5000,
            height: '500px',
            animation: 'fade'
        },
        'text-block': {
            type: 'text-block',
            heading: '',
            content: '',
            alignment: 'center',
            backgroundColor: '#ffffff',
            padding: '60px 20px'
        },
        'stats': {
            type: 'stats',
            backgroundColor: '#f8f9fa',
            padding: '40px 20px',
            items: [],
            layout: 'horizontal'
        },
        'recent-posts': {
            type: 'recent-posts',
            heading: '最新記事',
            sourceTable: 'blog_post',
            limit: 6,
            orderBy: 'created_at',
            orderDirection: 'DESC',
            whereClause: "status = 'published'",
            displayFields: {
                titleField: 'title',
                dateField: 'created_at',
                excerptField: 'excerpt',
                imageField: 'hero_image',
                tagsField: 'tags'
            },
            linkPattern: '#!/screen/blog/detail/{{id}}',
            layout: 'grid',
            columns: 3
        },
        'category-grid': {
            type: 'category-grid',
            heading: 'カテゴリ',
            sourceTable: 'blog_category',
            displayFields: {
                nameField: 'name',
                descriptionField: 'description'
            },
            columns: 3,
            linkPattern: '#!/screen/blog?category={{id}}',
            backgroundColor: '#ffffff',
            padding: '40px 20px'
        },
        'custom-html': {
            type: 'custom-html',
            content: '<div>カスタムHTML</div>'
        }
    };
    
    // 初期化
    $scope.init = function() {
        $scope.loadConfig();
    };
    
    // 設定を読み込む
    $scope.loadConfig = function() {
        $scope.loading = true;
        $http.get('/api/home/config').then(function(resp) {
            $scope.config = resp.data;
            var configObj = JSON.parse(resp.data.config_json);
            $scope.sections = configObj.sections || [];
            $scope.jsonText = JSON.stringify(configObj, null, 2);
            $scope.loading = false;
        }).catch(function(err) {
            console.error('Failed to load config:', err);
            $scope.error = '設定の読み込みに失敗しました';
            $scope.loading = false;
        });
    };
    
    // セクションを追加
    $scope.addSection = function(type) {
        if (!type) {
            type = prompt('セクションタイプを選択してください:\nhero, text-block, stats, recent-posts, category-grid, custom-html');
        }
        
        if (type && $scope.sectionTemplates[type]) {
            var newSection = angular.copy($scope.sectionTemplates[type]);
            $scope.sections.push(newSection);
            $scope.syncJsonText();
        }
    };
    
    // セクションを削除
    $scope.removeSection = function(index) {
        if (confirm('このセクションを削除しますか？')) {
            $scope.sections.splice(index, 1);
            $scope.syncJsonText();
        }
    };
    
    // セクションを上に移動
    $scope.moveSectionUp = function(index) {
        if (index > 0) {
            var temp = $scope.sections[index];
            $scope.sections[index] = $scope.sections[index - 1];
            $scope.sections[index - 1] = temp;
            $scope.syncJsonText();
        }
    };
    
    // セクションを下に移動
    $scope.moveSectionDown = function(index) {
        if (index < $scope.sections.length - 1) {
            var temp = $scope.sections[index];
            $scope.sections[index] = $scope.sections[index + 1];
            $scope.sections[index + 1] = temp;
            $scope.syncJsonText();
        }
    };
    
    // JSONテキストを同期
    $scope.syncJsonText = function() {
        var configObj = {
            title: 'ホーム',
            sections: $scope.sections
        };
        $scope.jsonText = JSON.stringify(configObj, null, 2);
    };
    
    // JSONテキストからセクションを更新
    $scope.syncFromJson = function() {
        try {
            var configObj = JSON.parse($scope.jsonText);
            $scope.sections = configObj.sections || [];
            $scope.error = null;
        } catch (e) {
            $scope.error = 'JSON形式が不正です: ' + e.message;
        }
    };
    
    // 保存
    $scope.save = function() {
        if ($scope.editMode === 'visual') {
            $scope.syncJsonText();
        } else {
            $scope.syncFromJson();
            if ($scope.error) return;
        }
        
        if (!confirm('ホーム画面設定を保存しますか？')) {
            return;
        }
        
        $scope.saving = true;
        $scope.error = null;
        $scope.success = null;
        
        var configObj = {
            title: 'ホーム',
            sections: $scope.sections
        };
        
        $http.put('/api/home/config', {
            config_json: JSON.stringify(configObj)
        }).then(function(resp) {
            $scope.success = resp.data.message || 'ホーム画面設定を保存しました';
            $scope.config = resp.data;
            
            setTimeout(function() {
                $scope.success = null;
                $scope.$apply();
            }, 3000);
        }).catch(function(err) {
            console.error('Failed to save config:', err);
            $scope.error = err.data && err.data.message || '保存に失敗しました';
        }).finally(function() {
            $scope.saving = false;
        });
    };
    
    // 履歴を読み込む
    $scope.loadHistory = function() {
        $http.get('/api/home/config/history').then(function(resp) {
            $scope.history = resp.data.records || [];
            $scope.showHistory = true;
        }).catch(function(err) {
            console.error('Failed to load history:', err);
            alert('履歴の読み込みに失敗しました');
        });
    };
    
    // バージョンを復元
    $scope.restoreVersion = function(version) {
        if (!confirm('バージョン ' + version + ' に復元しますか？')) {
            return;
        }
        
        $http.post('/api/home/config/restore/' + version).then(function(resp) {
            alert(resp.data.message || '復元しました');
            $scope.showHistory = false;
            $scope.loadConfig();
        }).catch(function(err) {
            console.error('Failed to restore version:', err);
            alert('復元に失敗しました');
        });
    };
    
    // プレビュー
    $scope.preview = function() {
        $scope.showPreview = true;
    };
    
    // 初期化実行
    $scope.init();
}]);

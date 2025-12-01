blogApp.controller('HomeController', ['$scope', '$http', function($scope, $http) {
    $scope.message = 'ブログシステムへようこそ';
    $scope.definition = null;
    $scope.recentPosts = [];
    $scope.loading = true;
    $scope.error = null;
    
    // ホーム画面の定義を読み込む
    $http.get('/api/screen/home/definition').then(function(defResp) {
        $scope.definition = defResp.data;
        
        // 最新記事セクションを読み込む
        var recentSection = $scope.definition.sections.find(function(s) {
            return s.type === 'recent-posts';
        });
        
        if (recentSection) {
            var params = {
                page: 0,
                pageSize: recentSection.limit || 5,
                status: 'published'  // 公開済み記事のみ
            };
            
            return $http.get('/api/screen/blog/data', { params: params });
        }
    }).then(function(dataResp) {
        if (dataResp) {
            $scope.recentPosts = dataResp.data.records || [];
        }
    }).catch(function(err) {
        console.error('Failed to load home definition:', err);
        $scope.error = 'ホーム画面の読み込みに失敗しました';
    }).finally(function() {
        $scope.loading = false;
    });
    
    // 記事詳細へのリンクを生成
    $scope.getPostLink = function(post) {
        return '#!/screen/blog/detail/' + post.id;
    };
}]);


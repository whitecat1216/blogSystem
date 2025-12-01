blogApp.controller('PostController', ['$scope', '$http', '$routeParams', '$location', function($scope, $http, $routeParams, $location) {
    $scope.post = null;
    $scope.definition = null;
    $scope.comments = [];
    $scope.newComment = {};
    $scope.loading = true;
    $scope.error = null;
    $scope.authenticated = false;
    $scope.username = null;
    $scope.isAdmin = false;
    $scope.commentSubmitting = false;
    $scope.commentSuccess = false;
    $scope.commentError = null;

    var id = $routeParams.id;
    
    // 認証状態を確認
    $http.get('/api/auth/status').then(function(resp){
        $scope.authenticated = !!resp.data.authenticated && resp.data.username !== 'anonymous';
        $scope.isAdmin = !!resp.data.isAdmin;
        $scope.username = resp.data.username;
        
        // 投稿者フィールドに自動入力
        if ($scope.authenticated) {
            $scope.newComment.author = $scope.username;
        }
    });
    
    // 定義を読み、表示項目をJSONで駆動
    $http.get('/api/screen/blog/definition').then(function(defResp){
        $scope.definition = defResp.data;
        return $http.get('/api/screen/blog/data/' + id);
    }).then(function(resp){
        $scope.post = resp.data;
        
        // コメント機能が有効な場合、コメントを読み込む
        if ($scope.definition.detailLayout && $scope.definition.detailLayout.comments && $scope.definition.detailLayout.comments.enabled) {
            loadComments();
        }
    }).catch(function(err){
        console.error('Failed to load post/definition:', err);
        $scope.error = '記事の読み込みに失敗しました';
    }).finally(function(){
        $scope.loading = false;
    });
    
    // コメント一覧を読み込む
    function loadComments() {
        var commentConfig = $scope.definition.detailLayout.comments;
        var params = {};
        params[commentConfig.foreignKey] = parseInt(id, 10);
        
        $http.get('/api/screen/comment/data', { params: params })
            .then(function(resp){
                $scope.comments = resp.data.records || [];
            })
            .catch(function(err){
                console.error('Failed to load comments:', err);
            });
    }
    
    // コメントを投稿
    $scope.submitComment = function() {
        if (!$scope.authenticated) {
            alert('ログインが必要です');
            return;
        }
        
        var commentConfig = $scope.definition.detailLayout.comments;
        var commentData = angular.copy($scope.newComment);
        commentData[commentConfig.foreignKey] = parseInt(id, 10); // post_idを数値として設定
        
        $scope.commentSubmitting = true;
        
        $http.post('/api/screen/comment/data', commentData)
            .then(function(){
                // コメント欄をクリア
                $scope.newComment = { author: $scope.username };
                // コメント一覧を再読み込み
                loadComments();
                
                // 成功メッセージを表示
                $scope.commentSuccess = true;
                setTimeout(function(){
                    $scope.commentSuccess = false;
                    $scope.$apply();
                }, 3000);
                
                // コメントセクションの先頭にスクロール
                setTimeout(function(){
                    var commentsElement = document.getElementById('comments');
                    if (commentsElement) {
                        commentsElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
                    }
                }, 300);
            })
            .catch(function(err){
                console.error('Failed to submit comment:', err);
                $scope.commentError = 'コメントの投稿に失敗しました。もう一度お試しください。';
                setTimeout(function(){
                    $scope.commentError = null;
                    $scope.$apply();
                }, 5000);
            })
            .finally(function(){
                $scope.commentSubmitting = false;
            });
    };
    
    // 記事編集画面へ移動（管理者のみ）
    $scope.editPost = function() {
        $location.path('/screen/blog');
        // 編集モードへの切り替えは dynamicScreenController で処理
    };
}]);
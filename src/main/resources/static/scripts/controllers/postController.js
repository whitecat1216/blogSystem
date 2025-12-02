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
                var allComments = resp.data.records || [];
                // 階層構造に変換
                $scope.comments = buildCommentTree(allComments);
            })
            .catch(function(err){
                console.error('Failed to load comments:', err);
            });
    }
    
    // コメントを階層構造に変換
    function buildCommentTree(comments) {
        var commentMap = {};
        var rootComments = [];
        
        // まずマップを作成
        comments.forEach(function(comment) {
            comment.replies = [];
            commentMap[comment.id] = comment;
        });
        
        // 親子関係を構築し、親の著者名を設定
        comments.forEach(function(comment) {
            if (comment.parent_id && commentMap[comment.parent_id]) {
                // 親コメントの著者名を設定
                var parentComment = commentMap[comment.parent_id];
                comment._parentAuthor = parentComment[$scope.definition.detailLayout.comments.authorField];
                commentMap[comment.parent_id].replies.push(comment);
            } else {
                rootComments.push(comment);
            }
        });
        
        return rootComments;
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
                // 返信状態をリセット
                $scope.replyingTo = null;
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
    
    // 返信開始
    $scope.startReply = function(comment) {
        $scope.replyingTo = comment;
        $scope.newComment = { 
            author: $scope.username,
            parent_id: comment.id
        };
        
        // 返信フォームにスクロール
        setTimeout(function(){
            var formElement = document.getElementById('comment-form');
            if (formElement) {
                formElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        }, 100);
    };
    
    // 返信キャンセル
    $scope.cancelReply = function() {
        $scope.replyingTo = null;
        $scope.newComment = { author: $scope.username };
    };
    
    // 記事編集画面へ移動（管理者のみ）
    $scope.editPost = function() {
        $location.path('/screen/blog');
        // 編集モードへの切り替えは dynamicScreenController で処理
    };
}]);
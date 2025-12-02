blogApp.controller('NavigationController', ['$scope', '$location', '$http', '$window', function($scope, $location, $http, $window) {
    $scope.isActive = function(viewLocation) {
        return viewLocation === $location.path();
    };
    $scope.isAdmin = false;
    $scope.authenticated = false;
    $scope.username = null;
    
    $scope.refreshAuth = function(){
        $http.get('/api/auth/status').then(function(resp){
            $scope.authenticated = !!resp.data.authenticated && resp.data.username !== 'anonymous';
            $scope.isAdmin = !!resp.data.isAdmin;
            $scope.username = resp.data.username;
        });
    };
    
    $scope.logout = function() {
        window.location.href = '/logout';
    };
    
    // ホーム画面編集モーダル
    $scope.editHomeConfig = function() {
        $http.get('/api/screen/home/definition').then(function(resp) {
            var jsonStr = JSON.stringify(resp.data, null, 2);
            var newConfig = prompt('ホーム画面のJSON設定を編集してください:\n\n(注意: 正しいJSON形式で入力してください)', jsonStr);
            
            if (newConfig && newConfig !== jsonStr) {
                try {
                    // JSONの妥当性チェック
                    var parsedConfig = JSON.parse(newConfig);
                    
                    // 保存確認
                    if (confirm('ホーム画面の設定を更新しますか？')) {
                        // home.jsonファイルを更新するAPIを呼び出す
                        // 注: 実際の実装では、ファイル更新APIが必要
                        alert('注意: この機能を有効にするには、サーバー側でhome.jsonファイルを書き換えるAPIの実装が必要です。\n\n現在の実装では、src/main/resources/screens/home.jsonを直接編集してください。');
                        
                        // デバッグ用: 整形されたJSONをコンソールに出力
                        console.log('新しいホーム画面設定:');
                        console.log(newConfig);
                    }
                } catch (e) {
                    alert('JSONの形式が正しくありません: ' + e.message);
                }
            }
        }).catch(function(err) {
            alert('ホーム画面設定の読み込みに失敗しました');
            console.error(err);
        });
    };
    
    $scope.refreshAuth();
}]);

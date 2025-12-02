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
        $window.location.href = '#!/home-editor';
    };
    
    $scope.refreshAuth();
}]);

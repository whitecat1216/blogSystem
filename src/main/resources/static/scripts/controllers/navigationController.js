blogApp.controller('NavigationController', ['$scope', '$location', '$http', function($scope, $location, $http) {
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
    
    $scope.refreshAuth();
}]);

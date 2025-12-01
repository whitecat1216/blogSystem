blogApp.controller('NavigationController', ['$scope', '$location', '$http', function($scope, $location, $http) {
    $scope.isActive = function(viewLocation) {
        return viewLocation === $location.path();
    };
    $scope.isAdmin = false;
    $scope.username = null;
    $scope.refreshAuth = function(){
        $http.get('/api/auth/status').then(function(resp){
            $scope.isAdmin = !!resp.data.isAdmin;
            $scope.username = resp.data.username;
        });
    };
    $scope.refreshAuth();
}]);

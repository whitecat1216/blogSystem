blogApp.controller('PostController', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {
    $scope.post = null;
    $scope.definition = null;
    $scope.loading = true;
    $scope.error = null;

    var id = $routeParams.id;
    // 定義を読み、表示項目をJSONで駆動
    $http.get('/api/screen/blog/definition').then(function(defResp){
        $scope.definition = defResp.data;
        return $http.get('/api/screen/blog/data/' + id);
    }).then(function(resp){
        $scope.post = resp.data;
    }).catch(function(err){
        console.error('Failed to load post/definition:', err);
        $scope.error = '記事の読み込みに失敗しました';
    }).finally(function(){
        $scope.loading = false;
    });
}]);
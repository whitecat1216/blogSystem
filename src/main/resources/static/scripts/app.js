// AngularJSアプリケーションの初期化
var blogApp = angular.module('blogApp', ['ngRoute']);

// ルーティング設定
blogApp.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
    $routeProvider
        .when('/', {
            templateUrl: '/views/home.html',
            controller: 'HomeController'
        })
        .when('/screen/:screenName', {
            templateUrl: '/views/dynamicScreen.html',
            controller: 'DynamicScreenController'
        })
        .when('/post/:id', {
            templateUrl: '/views/post.html',
            controller: 'PostController'
        })
        .otherwise({
            redirectTo: '/'
        });
}]);

// HTTP設定（Basic認証用）
blogApp.config(['$httpProvider', function($httpProvider) {
    $httpProvider.defaults.headers.common['Authorization'] = 'Basic ' + btoa('admin:admin123');
}]);

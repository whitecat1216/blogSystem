blogApp.controller('HomeController', ['$scope', function($scope) {
    $scope.message = 'ブログシステムへようこそ';
    $scope.description = 'JSON定義ファイルで画面を自動生成するブログシステムです。';
    
    $scope.features = [
        {
            title: '記事管理',
            description: 'ブログ記事の作成、編集、削除が可能です。',
            link: '#!/screen/blog'
        },
        {
            title: 'コメント管理',
            description: '記事に対するコメントを管理できます。',
            link: '#!/screen/comment'
        },
        {
            title: 'カテゴリ管理',
            description: '記事のカテゴリを管理できます。',
            link: '#!/screen/category'
        }
    ];
}]);

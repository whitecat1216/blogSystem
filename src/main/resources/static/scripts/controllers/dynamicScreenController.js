blogApp.controller('DynamicScreenController', ['$scope', '$http', '$routeParams', 
    function($scope, $http, $routeParams) {
        
    $scope.screenName = $routeParams.screenName;
    $scope.definition = null;
    $scope.records = [];
    $scope.searchParams = {};
    $scope.currentPage = 0;
    $scope.totalPages = 0;
    $scope.totalRecords = 0;
    $scope.pageSize = 10;
    $scope.isAdmin = false;
    
    $scope.editMode = false;
    $scope.currentRecord = {};
    $scope.isNewRecord = false;
    
    // 画面定義の読み込み
    $scope.loadDefinition = function() {
        // 認証ステータス確認してUI制御
        $http.get('/api/auth/status').then(function(resp){
            $scope.isAdmin = !!resp.data.isAdmin;
        });
        $http.get('/api/screen/' + $scope.screenName + '/definition')
            .then(function(response) {
                $scope.definition = response.data;
                $scope.pageSize = $scope.definition.pagination.pageSize || 10;
                // preload multiselect sources
                if ($scope.definition.formFields) {
                    $scope.definition.formFields.forEach(function(f){
                        if (f.type === 'multiselect' && f.source) {
                            $http.get(f.source).then(function(r){
                                // expecting [{id:..., name:...}]
                                f._options = r.data;
                            });
                        }
                    });
                }
                $scope.searchData();
            })
            .catch(function(error) {
                console.error('Definition load error:', error);
                alert('画面定義の読み込みに失敗しました');
            });
    };
    
    // データ検索
    $scope.searchData = function() {
        var params = {
            page: $scope.currentPage,
            pageSize: $scope.pageSize
        };
        
        // 検索パラメータを追加
        for (var key in $scope.searchParams) {
            if ($scope.searchParams[key]) {
                params[key] = $scope.searchParams[key];
            }
        }
        
        $http.get('/api/screen/' + $scope.screenName + '/data', { params: params })
            .then(function(response) {
                $scope.records = response.data.records;
                $scope.totalRecords = response.data.total;
                $scope.totalPages = Math.ceil($scope.totalRecords / $scope.pageSize);
                
                // blog記事の場合、コメント数を取得
                if ($scope.screenName === 'blog' && $scope.records.length > 0) {
                    loadCommentCounts();
                }
            })
            .catch(function(error) {
                console.error('Search error:', error);
                alert('データの検索に失敗しました');
            });
    };
    
    // 検索条件リセット
    $scope.resetSearch = function() {
        $scope.searchParams = {};
        $scope.currentPage = 0;
        $scope.searchData();
    };
    
    // ページ変更
    $scope.goToPage = function(page) {
        if (page >= 0 && page < $scope.totalPages) {
            $scope.currentPage = page;
            $scope.searchData();
        }
    };
    
    // 新規作成モード
    $scope.createNew = function() {
        $scope.currentRecord = {};
        $scope.isNewRecord = true;
        $scope.editMode = true;
        setTimeout(initRichEditors, 0);
    };
    
    // 編集モード
    $scope.editRecord = function(record) {
        $scope.currentRecord = angular.copy(record);
        // 内部フィールドは編集対象から除外
        Object.keys($scope.currentRecord).forEach(function(k){
            if (k.indexOf('_') === 0) {
                delete $scope.currentRecord[k];
            }
        });
        $scope.isNewRecord = false;
        $scope.editMode = true;
        setTimeout(initRichEditors, 0);
    };
    
    // 保存
    $scope.save = function() {
        if ($scope.isNewRecord) {
            // 新規作成
            $http.post('/api/screen/' + $scope.screenName + '/data', $scope.currentRecord)
                .then(function() {
                    alert('保存しました');
                    $scope.editMode = false;
                    $scope.searchData();
                })
                .catch(function(error) {
                    console.error('Create error:', error);
                    alert('保存に失敗しました');
                });
        } else {
            // 更新
            $http.put('/api/screen/' + $scope.screenName + '/data/' + $scope.currentRecord.id, 
                     $scope.currentRecord)
                .then(function() {
                    alert('更新しました');
                    $scope.editMode = false;
                    $scope.searchData();
                })
                .catch(function(error) {
                    console.error('Update error:', error);
                    console.error('Error details:', error.data);
                    console.error('Status:', error.status);
                    console.error('Record being updated:', $scope.currentRecord);
                    alert('更新に失敗しました: ' + (error.data?.message || error.statusText || 'Unknown error'));
                });
        }
    };
    
    // 削除
    $scope.deleteRecord = function(record) {
        if (confirm('本当に削除しますか?')) {
            $http.delete('/api/screen/' + $scope.screenName + '/data/' + record.id)
                .then(function() {
                    alert('削除しました');
                    $scope.searchData();
                })
                .catch(function(error) {
                    console.error('Delete error:', error);
                    alert('削除に失敗しました');
                });
        }
    };
    
    // キャンセル
    $scope.cancel = function() {
        $scope.editMode = false;
        $scope.currentRecord = {};
    };
    
    // コメント数を取得(blog記事用)
    function loadCommentCounts() {
        $scope.records.forEach(function(record) {
            $http.get('/api/screen/comment/data', {
                params: { 
                    post_id: record.id,
                    page: 0,
                    pageSize: 999
                }
            }).then(function(resp) {
                console.log('Comments for post', record.id, ':', resp.data);
                record._commentCount = resp.data.total || 0;
            }).catch(function(err) {
                console.error('Failed to load comments for post', record.id, err);
                record._commentCount = 0;
            });
        });
    }
    
    // 初期化
    $scope.loadDefinition();
        
        // File upload handler for fields with type 'file'
        $scope.onFileSelected = function(field, files) {
            if (!files || files.length === 0) return;
            var file = files[0];
            if (field.accept && !file.type.match(field.accept.replace('*','.*'))) {
                alert('許可されていないファイルタイプです');
                return;
            }
            if (field.maxSizeMb && file.size > field.maxSizeMb * 1024 * 1024) {
                alert('ファイルサイズが大きすぎます');
                return;
            }
            var endpoint = field.uploadEndpoint || '/api/upload';
            var formData = new FormData();
            formData.append('file', file);
            $http.post(endpoint, formData, {
                transformRequest: angular.identity,
                headers: { 'Content-Type': undefined }
            }).then(function(resp) {
                var url = resp.data.url;
                $scope.currentRecord[field.key] = url;
            });
        };

        // add new option for multiselect (tags/categories)
        $scope.addNewOption = function(field, newName) {
            if (!newName || !field.allowCreate) return;
            
            // フィールドのsourceからエンドポイントを判定
            var endpoint = '/api/tag/ensure'; // デフォルトはタグ
            if (field.source && field.source.includes('category')) {
                endpoint = '/api/category/ensure';
            } else if (field.source && field.source.includes('tag')) {
                endpoint = '/api/tag/ensure';
            }
            
            console.log('Adding new option:', newName, 'to endpoint:', endpoint);
            
            $http.post(endpoint, {name: newName}).then(function(resp){
                console.log('Successfully created:', resp.data);
                if (!field._options) field._options = [];
                field._options.push(resp.data);
                if (!$scope.currentRecord[field.key]) {
                    $scope.currentRecord[field.key] = [];
                }
                $scope.currentRecord[field.key].push(resp.data.id);
                field._newName = '';
            }).catch(function(error) {
                console.error('Failed to create new option:', error);
                console.error('Error response:', error.data);
                console.error('Error status:', error.status);
                alert('追加に失敗しました: ' + (error.data?.message || error.statusText || 'サーバーエラー'));
            });
        };

        // initialize Quill editors and bind to currentRecord
        function initRichEditors(){
            if (!$scope.definition || !$scope.definition.formFields) return;
            $scope.definition.formFields.forEach(function(field){
                if (field.type === 'richtext'){
                    var el = document.getElementById('quill_'+field.key);
                    if (!el) return;
                    // Quillエディタ初期化（太字/見出し/リスト等のツールバー追加）
                    var q = new Quill(el, {
                        theme: 'snow',
                        modules: {
                            toolbar: [
                                [{ header: [1, 2, 3, false] }],
                                ['bold', 'italic', 'underline', 'strike'],
                                ['blockquote', 'code-block'],
                                [{ list: 'ordered' }, { list: 'bullet' }],
                                [{ indent: '-1' }, { indent: '+1' }],
                                [{ color: [] }, { background: [] }],
                                [{ align: [] }],
                                ['clean']
                            ]
                        }
                    });
                    var html = $scope.currentRecord[field.key] || '';
                    if (html){
                        q.clipboard.dangerouslyPasteHTML(html);
                    }
                    q.on('text-change', function(){
                        var htmlContent = el.querySelector('.ql-editor').innerHTML;
                        $scope.$applyAsync(function(){
                            $scope.currentRecord[field.key] = htmlContent;
                        });
                    });
                }
            });
        }
}]);

var username = "test";
var username = "test";
var url = "http://localhost:8080/BookShop/api/";
var shownData;

$().ready(function () {
    init();
});


function init() {
    $("#btnSearch").on("click", searchBtn);
    $("#btnAdd").on("click", addBtn);
    $("#btnBuy").on("click", buyBtn);
    $("#btnPopulate").on("click", popBtn);
}

function searchBtn() {
    var title = $("#inputTitle").val();
    var author = $("#inputAuthor").val();
    $.ajax({
        type: "GET",
        url: url+"books?title=" + title + "&author=" + author,
        dataType: 'json',
        headers: {
            "Authorization": "Basic " + btoa(username + ":" + username) //not needed for GET
        },
        success: function (data) {
            shownData = data;
            populateTable();
        }
    });
}

function populateTable() {
    var tBody = $("#bookTableBody");
    tBody.children().remove(); //clear
    for (var i = 0; i < shownData.length; i++) {
        var input = "<input type='checkbox' name='buy' value=" + i + " />";
        var row = Mustache.render("<tr><td>{{title}}</td><td>{{author}}</td><td>{{price}}</td><td>" + input + "</td></tr>", shownData[i]);
        tBody.append(row);
    }
}

function addBtn() {

    var addBook = {};
    addBook.title = $("#inputTitle").val();
    addBook.author = $("#inputAuthor").val();
    addBook.price = Number($("#inputPrice").val());
    addBook.quantity = Number($("#inputQuantity").val());
    $.ajax({
        type: "POST",
        url: url+"book/",
        dataType: 'json',
        headers: {
            "Authorization": "Basic " + btoa(username + ":" + username)
        },
        data: JSON.stringify(addBook),
        statusCode: {
            201: function () {
                alert("created");
            },
            401: function (){
                alert("Bad credentials!");
            }
        }
    });
}

function buyBtn() {
    var $boxes = $('input[name=buy]:checked');
    var selectedBooks = [];
    var selectedRows = []; //to get colors on rows
    $boxes.each(function () {
        selectedBooks.push(shownData[this.value]);
        selectedRows.push(this.parentElement.parentElement);
    });
    $.ajax({
        type: "POST",
        url: url+"book/buy",
        dataType: 'json',
        headers: {
            "Authorization": "Basic " + btoa(username + ":" + username)
        },
        data: JSON.stringify(selectedBooks),
        success: function (data) {
            //add colors  
            for (var i = 0; i < data.results.length; i++) {
                var value = Object.values(data.results[i]);
                if (value[0] === "OK") {
                    selectedRows[i].classList.add("has-background-success");
                } else if (value[0] === "NOT_IN_STOCK") {
                    selectedRows[i].classList.add("has-background-warning");
                } else if (value[0] === "DOES_NOT_EXIST") {
                    selectedRows[i].classList.add("has-background-danger");
                }
            }
            //show buy-info
            $("#buyStatus").show();
            $("#totalPrice").html("Total cost: " + data.TotalPrice + " SEK");
        }
    });

}

function popBtn(){
      $.ajax({
        type: "POST",
        url: url+"books/external",
        dataType: 'json',
        headers: {
            "Authorization": "Basic " + btoa(username + ":" + username)
        },
        statusCode: {
            201: function () {
                alert("created");
            },
            500: function (){
                alert("Server Error!");
            }
        }
    });
    
}


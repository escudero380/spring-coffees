$(function () {

    // display form at start on validation failures (non-empty 'formAction' field
    // indicates that earlier submitted form needs to be amended and re-submitted)
    var action = $("#coffees-form input[name='formAction']").val();
    if (action !== "") {
        switch (action) {
            case "delete":
                switchFormToDelete();
                break;
            case "update":
                switchFormToUpdate();
                break;
            case "save":
                switchFormToSave();
                break;
            case "buy":
                switchFormToBuy();
                break;
            case "sell":
                switchFormToSell();
                break;
        }
        $("#coffees-form").show();
    }

    // handle filters options change
    $(".filters select").on("change", function () {
        var filtersUrl = $("#coffees-form").prop("action").replace(/\/[^\/]*$/, "/filters?");
        $(".filters select").each(function () {
            filtersUrl += $(this).prop("name") + "=" + $(this).val() + "&";
        });
        window.location.href = filtersUrl;
    });

    // handle 'select-row' radio click
    $("#coffees-table input[name='select-row']").on("click", function () {
        var actionURL = $("#coffees-form").prop("action");
        if (/\/(delete|update|buy|sell)/.test(actionURL))
            showForm();
    });

    // handle 'delete-button' click
    $("#delete-button").on("click", function () {
        switchFormToDelete();
        showForm();
    });

    function switchFormToDelete() {
        $("#coffees-form").prop("action", $("#coffees-form").prop("action").replace(/\/[^\/]*$/, "/delete"));
        $("#coffees-form input[name='formAction']").val("delete");
        $("#coffees-form span.notice").text("Are you sure you want to delete this coffee?");
        $("#coffees-form input, #coffees-form select").prop("disabled", true);
        $("#coffees-form label[for='quantity'], #coffees-form input[name='quantity']").hide();
    }

    // handle 'update-button' click
    $("#update-button").on("click", function () {
        switchFormToUpdate();
        showForm();
    });

    function switchFormToUpdate() {
        $("#coffees-form").prop("action", $("#coffees-form").prop("action").replace(/\/[^\/]*$/, "/update"));
        $("#coffees-form input[name='formAction']").val("update");
        $("#coffees-form span.notice").text("Edit selected coffee and submit changes.");
        $("#coffees-form input[name!='stock'], #coffees-form select").prop("disabled", false);
        $("#coffees-form label[for='quantity'], #coffees-form input[name='quantity']").hide();
    }

    // handle 'add-new-button' click
    $("#add-new-button").on("click", function () {
        switchFormToSave();
        $("#coffees-form input[name='id']").val(0);
        $("#coffees-form input[type='text']:not([name='quantity'])").val("");
        $("#coffees-form select[name='supplierId']").prop("selectedIndex", 0);
        $("#coffees-form input[name='stock']").val(0);
        $("#coffees-form").fadeIn(250);
    });

    function switchFormToSave() {
        $("#coffees-form").prop("action", $("#coffees-form").prop("action").replace(/\/[^\/]*$/, "/save"));
        $("#coffees-form input[name='formAction']").val("save");
        $("#coffees-form span.notice").text("New coffee will be added to the database.");
        $("#coffees-form input:not([name='stock']), #coffees-form select").prop("disabled", false);
        $("#coffees-form label[for='quantity'], #coffees-form input[name='quantity']").hide();
    }

    // handle 'buy-button' click
    $("#buy-button").on("click", function () {
        switchFormToBuy();
        showForm();
    });

    function switchFormToBuy() {
        $("#coffees-form").prop("action", $("#coffees-form").prop("action").replace(/\/[^\/]*$/, "/buy"));
        $("#coffees-form input[name='formAction']").val("buy");
        $("#coffees-form span.notice").text("The entered quantity will be added to the stock.");
        $("#coffees-form input, #coffees-form select").prop("disabled", true);
        $("#coffees-form input[name='quantity']").prop("disabled", false);
        $("#coffees-form label[for='quantity']").html(" &#10133; ");
        $("#coffees-form label[for='quantity'], #coffees-form input[name='quantity']").show();
    }

    // handle 'sell-button' click
    $("#sell-button").on("click", function () {
        switchFormToSell();
        showForm();
    });

    function switchFormToSell() {
        $("#coffees-form").prop("action", $("#coffees-form").prop("action").replace(/\/[^\/]*$/, "/sell"));
        $("#coffees-form input[name='formAction']").val("sell");
        $("#coffees-form span.notice").text("The entered quantity will be subtracted from the stock.");
        $("#coffees-form input, #coffees-form select").prop("disabled", true);
        $("#coffees-form input[name='quantity']").prop("disabled", false);
        $("#coffees-form label[for='quantity']").html(" &#10134; ");
        $("#coffees-form label[for='quantity'], #coffees-form input[name='quantity']").show();
    }

    // handle form buttons clicks
    $("#coffees-form").on("submit", function () {
        $(this).hide();
        $(".loader-wrapper").show();
        $("#coffees-form input, #coffees-form select").prop("disabled", false);
    });

    $("#coffees-form button.cancel").on("click", function () {
        var usersUrl = $("#coffees-form").prop("action").replace(/\/[^\/]*$/, "");
        window.location.href = usersUrl;
    });

    // helper function for form auto-filling and showing
    function showForm() {
        var selectedRow = $("#coffees-table input[name='select-row']:checked").closest("tr");
        if (selectedRow.length === 0) {
            $("#coffees-form").hide();
            alert("No row selected!");
        } else {
            $("#coffees-form input[name='id']").val(selectedRow.attr("data-coffee-id"));
            $("#coffees-form input[name='name']").val(selectedRow.find("td:nth-child(2)").text());
            $("#coffees-form input[name='origin']").val(selectedRow.find("td:nth-child(3)").text());
            var supplierId = selectedRow.find("td:nth-child(4)").attr("data-supplier-id");
            $("#coffees-form #supplierId option[value=" + supplierId + "]").prop("selected", true);
            $("#coffees-form input[name='currentPrice']").val(selectedRow.find("td:nth-child(5)").text());
            $("#coffees-form input[name='stock']").val(selectedRow.find("td:nth-child(6)").text());
            $("#coffees-form").fadeIn(250);
        }
    }

});

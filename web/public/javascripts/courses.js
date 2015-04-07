$(document).ready(function() {
    $('.course').click(function() {
        $('.course').removeClass("selected");
        $(this).addClass("selected");
        $("#info-pane").attr("value", $(this).children('.course-id').html());
        $("#info-pane .title").attr("href", $(this).children('.plan-link').html());
        $("#info-pane .title").html($(this).children('.name').html());
        $("#info-pane #info").html($(this).children('.info').html());
        $("#info-pane").show();
        $("#nav-tools").show();
    });

    var $info = $("#switch-info");
    var $tutors = $("#switch-tutors");

    $info.click(function() {
        $tutors.removeClass("selected");
        $info.addClass("selected");
        $("#info").show();
    })

    $tutors.click(function() {
        $("#info").hide();
        $info.removeClass("selected");
        $tutors.addClass("selected");
    })

    $("#tutor-start").click(function() {
        $.ajax({
            type: "POST",
            url: "/tutor",
            data: JSON.stringify({"course": $("#info-pane").attr("value")}),
            contentType: "application/json"
         })
         .done(function() {
            console.log("Success!")
         })
        .fail(function() {
            console.log("Fail");
        });

    });
});
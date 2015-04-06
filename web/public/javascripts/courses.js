$(document).ready(function() {
    $('.course').click(function() {
        $('.course').removeClass("selected");
        $(this).addClass("selected");
        $("#info-pane .title").attr("href", $(this).children('.plan-link').html());
        $("#info-pane .title").html($(this).children('.name').html());
        $("#info-pane #info").html($(this).children('.info').html());
        $("#info-pane").show();
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
});
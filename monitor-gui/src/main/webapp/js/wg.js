var WG = {};
WG.remoteHost = "http://localhost:9999/";
WG.ajax = function (ajaxOpts) {
    $.ajax({
        url: WG.remoteHost + ajaxOpts.url,
        type: ajaxOpts.type || "POST",
        data: JSON.stringify(ajaxOpts.data || ""),
        dataType: "json",
        contentType: ajaxOpts.contentType || "application/json; charset=UTF-8",
        headers: $.extend({
            jwtToken: $.cookie(WG.ajax.jwtTokenKey)
        }, ajaxOpts.headers || {}),
        success: function (response) {
            if (response && response.code !== undefined) {
                if (response.code === WG.ajax.successCode) {
                    if (ajaxOpts.success && ajaxOpts.success.constructor === Function) {
                        ajaxOpts.success(response.data, response);
                    }
                } else {
                    if (ajaxOpts.error && ajaxOpts.error.constructor === Function) {
                        ajaxOpts.error(response)
                    }
                }
            } else {
                WG.warning(WG.ajax.errorMessage);
            }
        },
        error: function () {
            WG.warning(WG.ajax.errorMessage);
        }
    })
};
WG.ajax.jwtTokenKey = "jwtToken";
WG.ajax.successCode = 9999;
WG.ajax.errorMessage = "服务器开小差了, 请稍候.....";

WG.success = function (message, timeout, nextCallBack) {
    WG._internal_alert(message, timeout, nextCallBack, "alert-success");
};

/**
 * window.alert
 * @param message
 * @param timeout
 * @param nextCallBack
 */
WG.warning = function (message, timeout, nextCallBack) {
    WG._internal_alert(message, timeout, nextCallBack, "alert-warning");
};

WG._internal_alert = function (message, timeout, nextCallBack, className) {
    timeout = timeout || 2000;
    nextCallBack = nextCallBack || function () {
    };
    var rid = "rid" + WG.randomStr();
    var template = '<div id="' + rid + '" class="alert-container alert ' + className + '">\n' +
        '\t<a href="#" class="close" data-dismiss="alert">\n' +
        '\t\t&times;\n' +
        '\t</a>\n' +
        '\t<strong>警告！</strong>' + message + '\n' +
        '</div>';
    $("body").append(template);
    window.setTimeout(function () {
        $("#" + rid).remove();
        nextCallBack();
    }, timeout);
};

WG.randomStr = function () {
    return Math.random().toString().replace("0.", "");
};

WG.date = function (date) {
    var d = new Date(date);
    return d.getFullYear()
        + "-" + (d.getMonth() < 10 ? ("0" + d.getMonth()) : d.getMonth())
        + "-" + (d.getDate() < 10 ? ("0" + d.getDate()) : d.getDate())
        + " " + (d.getHours() < 10 ? ("0" + d.getHours()) : d.getHours())
        + ":" + (d.getMinutes() < 10 ? ("0" + d.getMinutes()) : d.getMinutes())
        + ":" + (d.getSeconds() < 10 ? ("0" + d.getSeconds()) : d.getSeconds());
};

WG.windowInterval = function (periodSeconds, func) {
    var handler = window.setInterval(function () {
        if (!WG.windowInterval.stop) {
            func.call();
        }
        if (WG.windowInterval.kill) {
            window.clearInterval(handler);
        }
    }, periodSeconds * 1000);
};
WG.windowInterval.stop = false;
WG.windowInterval.kill = false;

WG.chartPie = function (response, elemId) {
    var option = {
        title: {
            text: response.title,
            left: "center",
            top: 20
        },
        tooltip: {
            trigger: "item",
            formatter: "{b} : {c} ({d}%)"
        },
        series: [
            {
                type: "pie",
                radius: "55%",
                center: ["50%", "50%"],
                data: response.series.sort(function (a, b) {
                    return a.value - b.value;
                }),
                roseType: "radius",
                animationType: "scale",
                animationEasing: "elasticOut",
                animationDelay: function (idx) {
                    return Math.random() * 200;
                }
            }
        ]
    };
    var pieChart = echarts.init(document.getElementById(elemId));
    pieChart.setOption(option);
    pieChart.on("click", function (params) {
        console.log(params);
    });
};
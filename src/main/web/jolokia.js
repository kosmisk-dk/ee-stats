var pie = (function (elementId) {
    var xy = function (fraction) {
        var angle = (fraction - .25) * 2.0 * Math.PI;
        return {
            x: Math.cos(angle),
            y: Math.sin(angle)
        };
    };

    var describeArc = function (from, to) {
        var start = xy(from);
        var end = xy(to);
        var large = to - from <= .5 ? 0 : 1;
        return [
            "M", start.x, start.y,
            "A", 1.0, 1.0, 0, large, 1, end.x, end.y,
            "L", 0.0, 0.0,
            "L", start.x, start.y
        ].join(" ");
    };
    var elem = function (type, kv) {
        var e = document.createElementNS('http://www.w3.org/2000/svg', type);
        e.setAttributeNS(null, "stroke-width", "0");
        for (var k in kv) {
            e.setAttributeNS(null, k, kv[k]);
        }
        return e;
    };
    var svg;
    var legend;

    var colors = [
        "#800", "#080", "#008", "#880", "#088", "#808",
        "#f00", "#0f0", "#00f", "#ff0", "#0ff", "#f0f",
        "#f88", "#8f8", "#88f", "#ff8", "#8ff", "#f8f"
    ];
    var order = [];
    var specs = {};
    var func;
    var render = function () {
        var total = 0.0;
        var show = [];
        order.forEach(function (k) {
            specs[k].use = false;
            if (specs[k].value !== undefined) {
                var num = specs[k].actual = func(specs[k].value);
                if (typeof (num) === 'number' && !Number.isNaN(num)) {
                    specs[k].use = true;
                    total += specs[k].actual;
                    show.push(specs[k]);
                }
            }
            specs[k].legend.style.display = specs[k].use ? "" : "none";
        });
        while (svg.hasChildNodes())
            svg.removeChild(svg.firstChild);
        if (show.length > 0) {
            var start;
            show.forEach(function (spec, i) {
                if (i === 0) {
                    svg.appendChild(elem('circle', {fill: spec.color, x: 0, y: 0, r: 1}));
                    start = spec.actual / total;
                } else {
                    var end = start + spec.actual / total;
                    svg.appendChild(elem('path', {fill: spec.color, d: describeArc(start, end)}));
                    start = end;
                }
            });
        } else {
            svg.appendChild(elem('circle', {fill: "grey", x: 0, y: 0, r: 1}));
        }
    };

    window.addEventListener('load', function () {
        var pie = document.getElementById(elementId);
        svg = elem("svg", {preserveAspectRatio: "xMidYMid slice", viewBox: "-1 -1 2 2"});
        pie.appendChild(svg);
        var legend_div = document.createElement('div');
        pie.appendChild(legend_div);

        legend = function (title, color) {
            var div = document.createElement("div");
            div.style.color = color;
            div.appendChild(document.createTextNode(title));
            legend_div.appendChild(div);
            return div;
        };
        var span = document.createElement('div');
        legend_div.appendChild(span);
        span.appendChild(document.createTextNode("legend:"));
        var select = document.createElement('select');
        span.appendChild(select);
        [
            {t: "count", f: function (e) {
                    return e['Count'];
                }},
            {t: "50% x count", f: function (e) {
                    return e['50thPercentile'] * e['Count'];
                }},
            {t: "50%", f: function (e) {
                    return e['50thPercentile'];
                }},
            {t: "75% x count", f: function (e) {
                    return e['75thPercentile'] * e['Count'];
                }},
            {t: "75%", f: function (e) {
                    return e['75thPercentile'];
                }},
            {t: "95% x count", f: function (e) {
                    return e['95thPercentile'] * e['Count'];
                }},
            {t: "95%", f: function (e) {
                    return e['95thPercentile'];
                }},
            {t: "98% x count", f: function (e) {
                    return e['98thPercentile'] * e['Count'];
                }},
            {t: "98%", f: function (e) {
                    return e['98thPercentile'];
                }},
            {t: "99% x count", f: function (e) {
                    return e['99thPercentile'] * e['Count'];
                }},
            {t: "99%", f: function (e) {
                    return e['99thPercentile'];
                }},
            {t: "99.9% x count", f: function (e) {
                    return e['999thPercentile'] * e['Count'];
                }},
            {t: "99.9%", f: function (e) {
                    return e['999thPercentile'];
                }},
            {t: "/1m", f: function (e) {
                    return e['OneMinuteRate'];
                }},
            {t: "/5m", f: function (e) {
                    return e['FiveMinuteRate'];
                }},
            {t: "/15m", f: function (e) {
                    return e['FifteenMinuteRate'];
                }}
        ].forEach(function (d) {
            var option = document.createElement('option');
            select.appendChild(option);
            option.value = d.f;
            option.appendChild(document.createTextNode(d.t));
        });
        var onchange = function () {
            func = eval("(" + select.options[select.selectedIndex].value + ")");
            render();
        };
        select.addEventListener("change", onchange);
        onchange();
    });
    return function (name) {
        if (!(name in specs)) {
            var color = colors[order.length % colors.length];
            specs[name] = {
                color: color,
                legend: legend(name.replace(/^name=/, ""), color)
            };
            order.push(name);
        }
        return (function (spec) {
            return function (value) {
                spec.value = value;
                render();
            };
        })(specs[name]);
    };
})("pie");


var jolokia = (function () {
    var order = "Count Min Mean Max 50thPercentile 75thPercentile 98thPercentile 95thPercentile 99thPercentile 999thPercentile MeanRate OneMinuteRate FiveMinuteRate FifteenMinuteRate StdDev RateUnit DurationUnit".split(" ");

    var get = function (url, func) {
        var r = new XMLHttpRequest();
        r.onreadystatechange = function () {
            if (r.readyState === 4) {
                if (r.status === 200) {
                    func(r.responseText);
//                } else {
//                    console.log(r);
                }
            }
        };
        r.open("GET", url);
        r.send();
    };
    var getJSON = function (url, func) {
        get(url, function (t) {
            func(JSON.parse(t));
        });
    };

    var set_values = function (div) {
        var name = div.getAttribute('data-name');
        var p = pie(name);
        var tbody = document.getElementById(name);
        return function (json) {
            if (div.classList.contains('closed'))
                return;
            p(json.value);
            order.forEach(function (prop) {
                var id = name + "+" + prop;
                var cell = document.getElementById(id);
                if (cell === null) {
                    var tr = document.createElement('tr');
                    tbody.appendChild(tr);
                    var td = document.createElement('td');
                    tr.appendChild(td);
                    td.appendChild(document.createTextNode(prop));
                    cell = document.createElement('td');
                    tr.appendChild(cell);
                    cell.setAttribute('id', id);
                }
                while (cell.hasChildNodes()) {
                    cell.removeChild(cell.firstChild);
                }
                cell.appendChild(document.createTextNode(json.value[prop] || "-"));
            });
        };
    };

    var update = function (div) {
        var func = set_values(div);

        return function () {
            getJSON("jolokia/read/metrics:" + div.getAttribute('data-name'), func);
        };
    };

    var place_div = function (div, parent_name) {
        var parent = document.getElementById(parent_name);
        var name = div.getAttribute('data-name');
        for (var child = parent.firstChild; child !== null; child = child.nextSibling) {
            var child_name = child.getAttribute('data-name');
            if (child_name !== null && child_name > name)
                break;
        }
        parent.insertBefore(div, child);
    };

    var toggle = function (div) {
        return function () {
            var parent_id = div.parentNode.getAttribute("id");
            if (parent_id === "closed") {
                var func = update(div);
                func();
                var i = window.setInterval(func, 5000);
                div.setAttribute('data-timer', i);
                place_div(div, "jolokia");
            } else {
                var i = div.getAttribute('data-timer');
                window.clearInterval(i);
                var name = div.getAttribute('data-name');
                pie(name)();
                place_div(div, "closed");
            }
        };
    };

    var list = function (json) {
        var base = document.getElementById("jolokia");
        var names = [];
        for (var name in json.value) {
            names.push(name);
        }
        var closed = document.createElement("div");
        closed.setAttribute('id', 'closed');
        base.appendChild(closed);
        names.sort().forEach(function (name) {
            var div = document.createElement("div");
            div.setAttribute("data-name", name);
            pie(name)();
            closed.appendChild(div);
            var span = document.createElement("span");
            div.appendChild(span);
            span.appendChild(document.createTextNode(("" + name).replace(/^name=/, "")));
            span.addEventListener("click", toggle(div));
            var table = document.createElement('table');
            div.appendChild(table);
            var tbody = document.createElement('tbody');
            tbody.setAttribute("id", name);
            table.appendChild(tbody);
        });
    };

    window.addEventListener("load", function () {
        getJSON("jolokia/list/metrics/", list);
        get("jolokia/name.txt", function (n) {
            document.title = n + "  - metric";
            var title = document.getElementById("title");
            title.appendChild(document.createTextNode(document.title));
        });
    });
    return undefined;
})();


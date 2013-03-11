function renderCalendar(nodeSelector, id) {
    // Make sure we clear out old data (if there is any)
    $(nodeSelector).empty();

    var width = 960,
        height = 136,
        cellSize = 17, // day cell size
        maxYear = new Date().getFullYear() + 1;

    var day = d3.time.format("%w"),
        week = d3.time.format("%U"),
        format = d3.time.format("%Y-%m-%d");

    var color = d3.scale.quantize()
        .domain([0, 4])
        .range(d3.range(4).map(function (d) {
            return "q" + d + "-4";
        }));

    var svg = d3.select(nodeSelector).selectAll("svg")
        .data(d3.range(2009, maxYear).reverse())
        .enter().append("svg")
        .attr("width", width)
        .attr("height", height)
        .attr("class", "RdYlGn")
        .append("g")
        .attr("transform", "translate(" + ((width - cellSize * 53) / 2) + "," + (height - cellSize * 7 - 1) + ")");

    svg.append("text")
        .attr("transform", "translate(-6," + cellSize * 3.5 + ")rotate(-90)")
        .style("text-anchor", "middle")
        .text(function (d) {
            return d;
        });

    var rect = svg.selectAll(".day")
        .data(function (d) {
            return d3.time.days(new Date(d, 0, 1), new Date(d + 1, 0, 1));
        })
        .enter().append("rect")
        .attr("class", "day")
        .attr("width", cellSize)
        .attr("height", cellSize)
        .attr("x", function (d) {
            return week(d) * cellSize;
        })
        .attr("y", function (d) {
            return day(d) * cellSize;
        })
        .datum(format);

    rect.append("title")
        .text(function (d) {
            return d;
        });

    svg.selectAll(".month")
        .data(function (d) {
            return d3.time.months(new Date(d, 0, 1), new Date(d + 1, 0, 1));
        })
        .enter().append("path")
        .attr("class", "month")
        .attr("d", monthPath);

    var url = "/" + id + "/posts"
    d3.json(url, function (error, json) {
        // Handle error cases
        if(error) {
            $(nodeSelector).empty()
            $(nodeSelector).append($("<h3 class='errorText'>" + error.responseText +"<h3/>"));
            return;
        }

        // Build user profile
        createUserProfile(json, nodeSelector)

        // Process user activity data
        var data = d3.nest()
            .key(function (d) {
                return d.date;
            })
            .rollup(function (d) {
                return {count: d[0].count, sampleText: d[0].sampleText, sampleId: d[0].sampleId};
            })
            .map(json.postData);

        // Render data
        rect.filter(function (d) {
            return d in data;
        })
            .attr("class", function (d) {
                return "day " + color(data[d].count);
            })
            .attr("onclick", function (d) {
                var sampleId = data[d].sampleId
                var userScreenName = json.userScreenName
                return "window.location.href = 'http://www.twitter.com/"+userScreenName+"/status/"+sampleId+"/';"
            })
            .select("title")
            .text(function (d) {
                var count = data[d].count
                var sampleText = data[d].sampleText
                if(count > 1) {
                    return d + ": " + count + " tweets. Sample:\n" + sampleText;
                } else {
                    return d + ": " + count + " tweet.\n" + sampleText;
                }
            })
    });

    function monthPath(t0) {
        var t1 = new Date(t0.getFullYear(), t0.getMonth() + 1, 0),
            d0 = +day(t0), w0 = +week(t0),
            d1 = +day(t1), w1 = +week(t1);
        return "M" + (w0 + 1) * cellSize + "," + d0 * cellSize
            + "H" + w0 * cellSize + "V" + 7 * cellSize
            + "H" + w1 * cellSize + "V" + (d1 + 1) * cellSize
            + "H" + (w1 + 1) * cellSize + "V" + 0
            + "H" + (w0 + 1) * cellSize + "Z";
    }

    d3.select(self.frameElement).style("height", "2910px");
}

function createUserProfile(userJson, parentSelector) {
    var userInfoDiv = $("<div class='userInfo'>")
    userInfoDiv.append("<img class='profileImageURL' width='73' height='73' src='" + userJson.profileImageURL + "'/>")
    userInfoDiv.append("<h2 class='statusesCount'>" + userJson.statusesCount+" Posts</h2>")
    $(parentSelector).prepend(userInfoDiv)
}
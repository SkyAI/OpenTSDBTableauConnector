var URL_INFO = {
    "host" : "localhost",
    "port" : "8080"
};

var URL_SUFFIX = {
    "query" : "/wdc/getData",
    "setConn" : "/wdc/setconn",
    "metrics" : "/wdc/metrics",
    "tagk" : "/wdc/tagk",
    "tagv" : "/wdc/tagv"
};

(function () {
    var myConnector = tableau.makeConnector();

    var tagKeyList = [];
    myConnector.getSchema = function (schemaCallback) {
        var connectionData = JSON.parse(tableau.connectionData);
        var metrics = connectionData["metrics"];
        var tagList = connectionData["tagList"];
        var cols = [
            { id : "timeStamp", alias : "datetime", dataType : tableau.dataTypeEnum.datetime }
        ];

        metrics.forEach(function(val){
            var split = val.split(".");
            var metricCol = {
                id: split[1],
                alias: split[1],
                dataType: tableau.dataTypeEnum.float
            }
            cols.push(metricCol);
        });

        tagList.forEach(function(val){
            for(var key in val){
                tagKeyList.push(key);
            }
        });
        tagKeyList.forEach(function(val){
            var tagCol = {
                id: val,
                alias: val,
                dataType: tableau.dataTypeEnum.string
            }
            cols.push(tagCol);
        })

        var tableInfo = {
                id : "OpenTSDB_WDC",
                alias : "Get OpenTSDB Data",
                columns : cols
            };
        schemaCallback([tableInfo]);
	};


    myConnector.getData = function (table, doneCallback) {
    	var connectionData = JSON.parse(tableau.connectionData);
		var metrics    = connectionData["metrics"];
		var startTime = connectionData["startTime"];
		var endTime   = connectionData["endTime"];
////		var tags  	  = connectionData["tags"];
		var host    = connectionData["url_host"];
		var port    = connectionData["url_port"];
		var tagList = connectionData["tagList"];

		tableData = [];
        queryUrl = buildUrl(host, port, URL_SUFFIX.query);
        param = buildPostParams(startTime, endTime, metrics, tagList);
//        tableau.abortWithError("Start to query.");

        $.ajax({
            url:queryUrl,
			type:'POST',
			dataType:'json',
			data: param,
            success:function(res){
//                tableau.abortWithError("Query Success.");
                data = res.data;
                if(res.resultCode == 200 && data != null && data.length != 0) {
                    data.forEach(function(val, id) {
                        date = val.timeStamp;
                        date = date > 20000000000 ? new Date(date) : new Date(date * 1000);
                        var entry = {
                             "timeStamp": date
                        };
                        tagKeyList.forEach(function(key){
                            if(val[key] != null){
                                entry[key] = val[key];
                            }
                        });
                        metrics.forEach(function(metricName){
                            var split = metricName.split(".");
                            entry[split[1]] = val.values[metricName];
                        });
                        tableData.push(entry);
                    });
                    doneGetData(tableData, table, doneCallback);
                } else {
                    str = "startTime : " + param.start;
                    str += "endTime : " + param.end;
                    tableau.abortWithError(str);
                }
            },
            error: function(d,msg) {
                var str = "";
                for (var key in d) {
                    str += key + " : " + d[key] + "\n";
                }
                str += "startTime : " + param.start;
                str += "endTime : " + param.end;
                tableau.abortWithError(str + msg);
            }
        });
    }

    tableau.registerConnector(myConnector);

    function doneGetData (tableData, table, doneCallback) {
        table.appendRows(tableData);
        doneCallback();
    };
    
    var setupConnector = function() {
        var metricList = $("#metricList").selectpicker('val');
		var startTime = $("#start_time").val().trim();
		var endTime   = $("#end_time").val().trim();
//		var tags  	  = $("#tags").val();
		var host    = URL_INFO.host;
		var port    = URL_INFO.port;
		var keySelector = $(".tagKey :selected");
		var valueSelector = $(".tagValue :selected");
		var tagList = [];
		for(var i = 0; i< keySelector.length; ++i) {
		    var keyVal = keySelector[i].value;
		    var valueVal = valueSelector[i].value;
		    var obj = {};
		    obj[keyVal] = valueVal;
		    tagList.push(obj);
		}
        
        if (metricList && parserDate(startTime) != null && parserDate(endTime) != null && metricList != null && metricList.length != 0) {
            var connectionData = {
    			"url_host": host,
    			"url_port": port,
    		    "startTime": startTime,
    			"endTime": endTime,
                "metrics": metricList,
                "tagList": tagList
            };
            tableau.connectionData = JSON.stringify(connectionData);
            return true;
        } else {
            alert("Missing Information.");
            return false;
        }
     };


    $(document).ready(function () {
        $("#submitButton").click(function() { // This event fires when a button is clicked
            if(setupConnector())
                tableau.submit();
        });
        $('#inputForm').submit(function(event) {
            event.preventDefault();
            if(setupConnector())
                tableau.submit();
        });
        $('#testButton').click(function(event) {
            var tableList = [];
            if(setupConnector())
                testFunction(tableList, tagKeyList);
        });
        $('#setUrlButton').click(function(event) {
            opentsdb_host = $("#url_host").val().trim();
            opentsdb_port = $("#url_port").val().trim();
            if(opentsdb_host != null && opentsdb_port != null && opentsdb_host.length != 0 && opentsdb_port.length != 0) {
                setViewAtr (opentsdb_host, opentsdb_port);
            } else {
                alert("Missing OpenTSDB host Info.");
            }
        });
    });
})()

function setViewAtr (opentsdb_host, opentsdb_port) {

    var setOpenTSDBConn = function () {
        var setConn = buildUrl(URL_INFO.host, URL_INFO.port, URL_SUFFIX.setConn);
        param = {
            "host": opentsdb_host,
            "port": opentsdb_port
        }
        $.ajax({
            url: setConn,
			type:'post',
			dataType : 'json',
			data: param,
            success: function(res) {
                alert('Set Connection to OpenTSDB Success');
                data = res.data;
                if(res.resultCode == 200 && data != null) {
                    return true;
                } else {
                    alert("Connect to OpenTSDB Failed.");
                    return false;
                }
            },
            error: function(d,msg) {
                alert(msg);
                return false;
            }
        });
    }

    var getMetricList = function () {
        var metricUrl = buildUrl(URL_INFO.host, URL_INFO.port, URL_SUFFIX.metrics);
        $.ajax({
            url: metricUrl,
			type:'get',
			dataType : 'json',
            success: function(res) {
                alert('Get Metrics Success');
                data = res.data;
                if(res.resultCode == 200 && data != null) {
                    data.forEach(function(val) {
                        addSelectHtml('#metricList' ,val);
                    });
                    setVisible();
                    return true;
                } else {
                    var str = "";
                    for (var key in res) {
                        str += key + " : " + res[key] + "\n";
                    }
                    alert("Error" + str);
                    return false;
                }
            },
            error: function(d,msg) {
                alert(msg);
                return false;
            }
        });
    }

    var tagKeys = [];
    var getTagKeyList = function () {
        var tagkUrl = buildUrl(URL_INFO.host, URL_INFO.port, URL_SUFFIX.tagk);
        var tagKeySelector = ".tagKeyList select";
        $.ajax({
            url: tagkUrl,
			type:'get',
			dataType : 'json',
            success: function(res) {
                alert('Get Tag Keys Success');
                data = res.data;
                if(res.resultCode == 200 && data != null) {
                    if (data instanceof Array){
                        tagKeys = data;
                    } else {
                        tagKeys.push(data);
                    }
                    return true;
                } else {
                    var str = "";
                    for (var key in res) {
                        str += key + " : " + res[key] + "\n";
                    }
                    alert("Error" + str);
                    return false;
                }
            },
            error: function(d,msg) {
                alert(msg);
                return false;
            }
        });
    }

    var tagValues = [];
    var getTagValueList = function () {
        var tagvUrl = buildUrl(URL_INFO.host, URL_INFO.port, URL_SUFFIX.tagv);
        var tagValueSelector = ".tagValueList select";
        $.ajax({
            url: tagvUrl,
			type:'get',
			dataType : 'json',
            success: function(res) {
                alert('Get Tag Values Success');
                data = res.data;
                if(res.resultCode == 200 && data != null) {
                    if (data instanceof Array){
                        tagValues = data;
                    } else {
                        tagValues.push(data);
                    }
                    return true;
                } else {
                    var str = "";
                    for (var key in res) {
                        str += key + " : " + res[key] + "\n";
                    }
                    alert("Error" + str);
                    return false;
                }
            },
            error: function(d,msg) {
                alert(msg);
                return false;
            }
        });
    }

    function addSelectHtml (selector, values) {
        if (values instanceof Array){
            values.forEach(function(val){
                $(selector).append("<option>" + val + "</option>");
            });
        } else {
            $(selector).append("<option>" + values + "</option>");
        }
        $(selector).selectpicker('refresh');
        $(selector).selectpicker('render');
    }

    var tagInfoBegin = "                <div class=\"row tagInfo\">"
                       +"                     <div class=\"col-md-6\">"
                       +"                         <select class=\"selectpicker show-tick form-control bs-select-hidden tagKey\""
                       +"                                 data-live-search=\"true\">";

    var tagInfoMiddle = "                        </select>"
                        +"                     </div>"
                        +"                     <div class=\"col-md-6\">"
                        +"                         <select class=\"selectpicker show-tick form-control bs-select-hidden tagValue\""
                        +"                                 data-live-search=\"true\">";

    var tagInfoEnd = "                     </select>"
                     +"                    </div>";
                     +"                </div>";
    var tagInfoKey = "";
    var tagInfoValue = "";
    $('#addTag').click(function(event) {
        if(tagInfoKey.length == 0) {
            tagKeys.forEach(function (val) {
                tagInfoKey += "<option>" + val + "</option>";
            });
        }
        if(tagInfoValue.length == 0) {
            tagValues.forEach(function (val) {
                tagInfoValue += "<option>" + val + "</option>";
            });
        }

        if(tagInfoKey.length != 0 && tagInfoValue.length != 0) {
            tagInfo = tagInfoBegin + tagInfoKey + tagInfoMiddle + tagInfoValue + tagInfoEnd;
            $('#tagTable').append(tagInfo);
            $("#tagTable select").selectpicker('refresh');
            $("#tagTable select").selectpicker('render');
        }
    });

    var setVisible = function () {
        if(getMetricList || getTagKeyList || getTagValueList) {
            document.getElementById("inputForm").style.visibility="visible";
            $('#inputForm select').selectpicker({
                size: 10
            });
        } else {
            $("#url_host").attr("readOnly",false);
            $("#url_port").attr("readOnly",false);
            document.getElementById("setUrlButton").style.visibility="visible";
            alert("Connect to OpenTSDB Failed");
        }
    }

    var setHidden = function () {
        $("#url_host").attr("readOnly",true);
        $("#url_port").attr("readOnly",true);
        document.getElementById("setUrlButton").style.visibility="hidden";
    }

    return {
        hidden : setHidden(),
        setConnection: setOpenTSDBConn(),
        metricView: getMetricList(),
        tagKeyView: getTagKeyList(),
        tagValueView: getTagValueList(),
        tagKeys : tagKeys,
        tagValues : tagValues
    };
}

function testFunction (tableData, tagKeyList) {

     function getData() {
    	var connectionData = JSON.parse(tableau.connectionData);
		var metrics    = connectionData["metrics"];
		var startTime = connectionData["startTime"];
		var endTime   = connectionData["endTime"];
////		var tags  	  = connectionData["tags"];
		var host    = connectionData["url_host"];
		var port    = connectionData["url_port"];
		var tagList = connectionData["tagList"];

		tableData = [];
        queryUrl = buildUrl(host, port, URL_SUFFIX.query);
        param = buildPostParams(startTime, endTime, metrics, tagList);
        $.ajax({
            url: queryUrl,
			type:'post',
			dataType : 'json',
			data: param,
            success: function(res) {
                alert('ok');
                data = res.data;
                if(data != null && data.length != 0) {
                    data.forEach(function(val, id) {
                        date = val.timeStamp;
                        if (date > 20000000000) {
                            date = new Date(date);
                        } else {
                            date = new Date(date*1000);
                        }
                        var entry = {
                             "timeStamp": date
                        };
                        tagKeyList.forEach(function(key){
                            if(val[key] != null){
                                entry[key] = val[key];
                            }
                        });
                        metrics.forEach(function(metricName){
                            var split = metricName.split(".");
                            entry[split[1]] = val.values[metricName];
                        });
                        tableData.push(entry);
                    });
                    test(tableData);
                } else {
                    str = "startTime : " + param.start;
                    str += "endTime : " + param.end;
                    alert(str);
                }
            },
            error: function(d,msg) {
                var str = "";
                for (var key in d) {
                    str += key + " : " + d[key] + "\n";
                }
                str += "startTime : " + param.start + "\n";
                str += "endTime : " + param.end + "\n";
                alert(str);
                alert(msg);
            }
        });
    }


    var test = function (tableData) {
        testData(tableData);
        dataView(tableData);
    }

    var testData = function (table) {
        var entry = {
            "timeStamp": "2017-07-31T07:34:34.000Z",
            "lalala1": 1.1,
            "lalala2": 1.2,
            "lalala3": 1.3
        }
        table.push(entry);
    };

    var dataView = function(data){
        var dataPanel = '';
        if (data instanceof Array){
            data.forEach(function(val,idx){
//                dataPanel += `<tr><th scope=\"row\">${idx+1}</th><td>${val.timeStamp}</td>`;
//                for(var key in val) {
//                    if("timeStamp"!=key){
//                        var value = val[key];
//                        dataPanel += `<td>${value}</td>`;
//                    }
//                };
//                dataPanel += `<td class=\"option\"></tr>`;
            })

            $(".data_list tbody").html(dataPanel);
            dataPanel = '';
        }

    };

    return {
        getData : getData(),
        testData : testData(tableData)
    }
}

function buildQueryUri(host, port, startTime, endTime, metric/*, tags*/) {
    var uri = buildUrl(host, port, URL_SUFFIX.query) + "?start=" + startTime
    			+ "&end=" + endTime + "&m=sum:" + metric;
    return uri;
}

function buildUrl (host, port, suffix) {
    return "http://" + host + ":" + port + suffix;
}

function buildOpenTSDBQueryParams( startTime, endTime, metrics/*, tags*/) {
    queries = [];
    for(var metric in metrics) {
        query = {
            "metric": metric,
            "aggregator": "sum"
        };
    }
    queries.push(query);

    parsedStart = parserDate(startTime).getTime();
    parsedEnd = parserDate(endTime).getTime();

	param = {
	    "start":startTime,
	    "end" : endTime,
        "queries": queries
	};
	return param;
}

function buildQueryParams( startTime, endTime, metrics) {
	var param = {
	    "start":parserDate(startTime),
	    "end" : parserDate(endTime),
	    metrics : JSON.stringify(metrics)
	};
	return param;
}

function buildPostParams( startTime, endTime, metrics, tagList) {
	var param = {
	    "start":parserDate(startTime),
	    "end" : parserDate(endTime),
	    "metrics" : JSON.stringify(metrics),
	    "tagList": JSON.stringify(tagList)
	};
	return param;
}

/* 支持parse 的Date的格式
 * 2017/07/31 15:34:00
*/
function parserDate (date) {  
    var t = Date.parse(date);
    if (isNaN(t)) {
        var parsedDate = new Date(Date.parse(date.replace(/-/g," ")));
        if(!isNaN(parsedDate))
            return parsedDate.getTime();
    } else {
        return t;
    }  
}

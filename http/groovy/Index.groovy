import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class Index extends Resource {
    public String generatePage() {
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)

        htmlBuilder.html() {
            head() {
                meta('http-equiv':"Content-Type", content:"text/html; charset=utf-8")
                title('Flot Examples: AJAX')
                link(href:'css/examples.css', rel:'stylesheet', type:'text/css')
                script(language:"javascript", type:"text/javascript", src:"js/jquery.js", '')
                script(language:"javascript", type:"text/javascript", src:"js/jquery.flot.js", '')
                script(language:"javascript") {
                    mkp.yieldUnescaped '''
	\$(function() {

		var options = {
			lines: {
				show: true
			},
			points: {
				show: true
			},
			xaxis: {
				tickDecimals: 0,
				tickSize: 1
			}
		};

		var data = [];

		\$.plot("#placeholder", data, options);

		// Fetch one series, adding to what we already have

		var alreadyFetched = {};

		\$("button.fetchSeries").click(function () {

			var button = \$(this);

			// Find the URL in the link right next to us, then fetch the data

			var dataurl = button.siblings("a").attr("href");

			function onDataReceived(series) {

				if (!alreadyFetched[series.label]) {
					alreadyFetched[series.label] = true;
					data.push(series);
				}

                var i = 0;
		\$.each(series, function(key, val) {
			val.color = i;
			++i;
		});

		// insert checkboxes 
		var choiceContainer = \$("#choices");
		\$.each(series, function(key, val) {
			choiceContainer.append("<br/><input type='checkbox' name='" + key +
				"' checked='checked' id='id" + key + "'></input>" +
				"<label for='id" + key + "'>"
				+ val.label + "</label>");
		});

		choiceContainer.find("input").click(plotAccordingToChoices);

		function plotAccordingToChoices() {

			var data = [];

			choiceContainer.find("input:checked").each(function () {
				var key = \$(this).attr("name");
				if (key && series[key]) {
					data.push(series[key]);
				}
			});

			if (data.length > 0) {
				\$.plot("#placeholder", data, {
					yaxis: {
						min: 0
					},
					xaxis: {
						tickDecimals: 0
					}
				});
			}
		}

		plotAccordingToChoices();
			}

			\$.ajax({
				url: dataurl,
				type: "GET",
				dataType: "json",
				success: onDataReceived
			});
		});

		// Initiate a recurring data update

		\$("button.dataUpdate").click(function () {

			data = [];
			alreadyFetched = {};

			\$.plot("#placeholder", data, options);

			var iteration = 0;

			function fetchData() {

				++iteration;

				function onDataReceived(series) {

					// Load all the data in one pass; if we only got partial
					// data we could merge it with what we already have.

					data = [ series ];
					\$.plot("#placeholder", data, options);
				}

				// Normally we call the same URL - a script connected to a
				// database - but in this case we only have static example
				// files, so we need to modify the URL.

				\$.ajax({
					url: "data.json",
					type: "GET",
					dataType: "json",
                    async: false,
					success: onDataReceived
				});

				if (iteration < 5) {
					setTimeout(fetchData, 1000);
				} else {
					data = [];
					alreadyFetched = {};
				}
			}

			setTimeout(fetchData, 1000);
		});

		// Load the first series by default, so we don't have an empty plot

		\$("button.fetchSeries:first").click();

		// Add the Flot version string to the footer

		\$("#footer").prepend("Flot " + \$.plot.version + " &ndash; ");
	});
'''
                }
            }
            body() {
                div(id:'header') {
                    h2('AJAX')
                }
                div(id:'content') {
                    div(class:"demo-container") {
                        div(id:"placeholder",class:"demo-placeholder", style: 'float:left; width:675px;','')
                        p(id:'choices', style:'float:right; width:135px', '')
                    }
                    p() {
                        button(class:"fetchSeries", 'First dataset')
                        a(href:'data.json', 'data')
                        span('')
                    }
                }
                div(id:'footer', 'Insert Text here') {
                }
            }
        }

        return String.format("<!DOCTYPE HTML>%n%s", writer)
    }
}

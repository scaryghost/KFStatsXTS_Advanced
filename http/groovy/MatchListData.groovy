import com.github.etsai.kfsxtrackingserver.DefaultReader.Order
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.json.JsonBuilder

public class MatchListData extends Resource {
    public String generatePage() {
        def builder= new JsonBuilder()
        def calendar= Calendar.getInstance(TimeZone.getTimeZone("GMT"))

        builder(
            cols: [[type: 'date', label: 'Date'], [type: 'number', label: 'Number of Games']],
            rows: reader.executeQuery("server_match_daily").collect {
                calendar.setTimeInMillis((long)it.date_end)
                def date= "Date(${[Calendar.YEAR, calendar.MONTH, calendar.DAY_OF_MONTH].collect{ calendar.get(it) }.join(',')})"
                [c: [[v: date], [v: it.count]]]
            }
        )
        return builder.toPrettyString()
    }
}

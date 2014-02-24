import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.json.JsonBuilder
import java.util.UUID

public class Data extends Resource {
    public String generatePage() {
        def builder= new JsonBuilder()
        builder(
            cols: [[type: 'string', label:'Role'], [type:'string', label:'Name'], [type:'date', label:'Start'], [type:'date', label:'End']],
            rows: reader.executeQuery("server_match_wave_times", UUID.fromString(queries.matchuuid)).collect {
                [c:[[v:"Wave"],[v:"Wave ${it.wave}"],[v:(long)it.time_begin],[v:(long)it.time_end]]]
            }
        )
        return builder.toPrettyString()
    }
}

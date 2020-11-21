import scaryghost.kfsxtrackingserver.web.Resource
import groovy.json.JsonBuilder
import java.util.UUID

public class Data extends Resource {
    public String generatePage() {
        def builder= new JsonBuilder()
        builder(
            label: "Wave Duration",
            data: reader.executeQuery("server_wave_summaries", UUID.fromString(queries.matchuuid)).collect {
                [it.wave, it.duration]
            }
        )
        return builder
    }
}

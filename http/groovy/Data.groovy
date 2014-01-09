import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.json.JsonBuilder

public class Data extends Resource {
    public String generatePage() {
        def builder= new JsonBuilder()
        builder(
            label: "Wave Duration",
            data: reader.executeQuery("server_match_durations", '3b4c0c99-5f51-4a61-b39e-f7e8d298e30c').collect {
                [it.wave, it.duration]
            }
        )
        return builder
    }
}

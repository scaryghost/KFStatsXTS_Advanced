import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.json.JsonBuilder

public class Data extends Resource {
    public String generatePage() {
        def builder= new JsonBuilder()
        builder(
            label: "Wave Duration",
            data: reader.executeQuery("server_wave_summaries", '532c7656-073a-4a38-8f19-3ea56f352321').collect {
                [it.wave, it.duration]
            }
        )
        return builder
    }
}

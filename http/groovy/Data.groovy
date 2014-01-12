import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.json.JsonBuilder

public class Data extends Resource {
    public String generatePage() {
        def builder= new JsonBuilder()
        builder(
            label: "Wave Duration",
            data: reader.executeQuery("server_wave_summaries", '49d51490-c7ff-4389-afa9-3ad12a0412d5').collect {
                [it.wave, it.duration]
            }
        )
        return builder
    }
}

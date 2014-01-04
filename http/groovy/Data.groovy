import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.json.JsonBuilder

public class Data extends Resource {
    public String generatePage() {
        def builder= new JsonBuilder()

        builder {
            label('Example')
            data([[1999, 3.0], [2000, 3.9], [2001, 2.0], [2002, 1.2], [2003, 1.3], [2004, 2.5], [2005, 2.0], [2006, 3.1], [2007, 2.9], [2008, 0.9]])
        }
        return builder
    }
}

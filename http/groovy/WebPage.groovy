import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public abstract class WebPage extends Resource {
    protected def pageTitle, jsSrcs= ['//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js'],
            cssSrcs= [], embeddedJs= []

    protected abstract void fillBody(builder)

    public String generatePage() {
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)

        htmlBuilder.html() {
            head() {
                meta('http-equiv':"Content-Type", content:"text/html; charset=utf-8")
                title(pageTitle)
                jsSrcs.each {
                    script(type:'text/javascript', src:it, '')
                }
                embeddedJs.each {js ->
                    script(type:'text/javascript') {
                        mkp.yieldUnescaped js
                    }
                }
                cssSrcs.each {
                    link(rel:"stylesheet", type:"text/css", href:it)
                }
            }
            body() {
                fillBody(htmlBuilder)
            }
        }

        return String.format("<!DOCTYPE HTML>%n%s", writer)
    }
}

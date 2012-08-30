package metridoc.admin

import org.apache.commons.lang.text.StrBuilder

/**
 * Created with IntelliJ IDEA.
 * User: dongheng
 * Date: 8/24/12
 * Time: 10:39 AM
 * To change this template use File | Settings | File Templates.
 */
class LogService {

    def grailsApplication
    public static final ONE_HOUR = 1000 * 60 * 60
    public static final SIX_HOURS = ONE_HOUR * 6
    public static final TWELVE_HOURS = SIX_HOURS * 2
    public static final ONE_DAY = TWELVE_HOURS * 2
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

    enum LineType {
        INFO, ERROR, WARN
    }


    public void renderLog(response, file) {


        def previous = LineType.INFO
        def previousDateClass = "all"
        file.eachLine {String line ->
            def escapedLine = escape(line)
            def div = addDiv(escapedLine, previous, previousDateClass)
            def divLine = div.line
            previous = div.previous
            previousDateClass = div.previousDateClass
            response << divLine
        }
    }

    public static String escape(String s) {
        StringBuilder builder = new StringBuilder();
        boolean previousWasASpace = false;
        for (char c : s.toCharArray()) {
            if (c == ' ') {
                if (previousWasASpace) {
                    builder.append("&nbsp;");
                    previousWasASpace = false;
                    continue;
                }
                previousWasASpace = true;
            } else {
                previousWasASpace = false;
            }
            switch (c) {
                case '<': builder.append("&lt;"); break;
                case '>': builder.append("&gt;"); break;
                case '&': builder.append("&amp;"); break;
                case '"': builder.append("&quot;"); break;
                case '\n': builder.append("<br/>"); break;
            // We need Tab support here, because we print StackTraces as HTML
                case '\t': builder.append("&nbsp; &nbsp; &nbsp;"); break;
                default:
                    if (c < 128) {
                        builder.append(c);
                    } else {
                        builder.append("&#").append((int) c).append(";");
                    }
            }
        }
        return builder.toString();
    }

    public LineType getLineType(line, previous) {
        if (line.contains(LineType.INFO.toString())) {
            return LineType.INFO
        }

        if (line.contains(LineType.ERROR.toString()) ||
                line.contains("Exception") ||
                line.contains("at ")) {
            return LineType.ERROR
        }

        if (line.contains(LineType.WARN.toString())) {
            return LineType.WARN
        }

        return previous
    }

    private static getDateClass(line, previousDate, now) {
        def m = line =~ /(\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2})/
        def result = "all"
        if(m.lookingAt()) {
            def date = Date.parse(DATE_FORMAT, m.group(1)).getTime()


            def dateTest = {
                def nowTime = now.time
                def difference = nowTime - it
                date > difference
            }

            if(dateTest(ONE_HOUR)) {
                result += "hour"
            }

            if(dateTest(SIX_HOURS)) {
                result += " sixHours"
            }

            if(dateTest(TWELVE_HOURS)) {
                result += " twelveHours"
            }

            if(dateTest(ONE_DAY)) {
                result += " day"
            }

            return result
        } else {
            return previousDate
        }
    }

    def addDiv(String line, previous, previousDateClass) {

        def dateClass = getDateClass(line, previousDateClass, new Date())
        def addLine = {clazz, color->
            clazz += " ${dateClass}"
            "<div class=\"content logLine ${clazz}\" style=\"color:${color}\">${line}</div>"
        }

        def result
        def type = getLineType(line, previous)
        switch (type) {
            case LineType.ERROR:
                result = addLine("error", "red")
                break
            case LineType.WARN:
                result = addLine("warn", "#CCCC66")
                break
            default:
                result = addLine("info", "green")
        }

        return [line: result, previous:type, previousDateClass:dateClass]

    }
}
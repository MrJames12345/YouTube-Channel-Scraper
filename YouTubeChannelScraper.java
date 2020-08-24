import java.util.*;
import java.io.*;
 // Webscraping
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;


class YouTubeChannelScraper
{

// - Variables - //

    // Data
    String channelUrl;
    String channelHtml;
    String channelName;
    // Max number of video to check
    int maxNumVids = 25;
    // Scraping
    private String channelType1Check = "}]},\"title\":{\"runs\":[{\"text\":\"";
    private String channelType2Check = "views\"}},\"simpleText\":\"";
    private String textAtChannelNameLeft    = "<meta name=\"title\" content=\"";
    private String textAtChannelNameRight   = "\">";
    private int    textAtChannelNameLeftLen = 28;
    private String textAtHrefLeft    = "\"url\":\"/watch?v=";
    private String textAtHrefRight   = "\"";
    private int    textAtHrefLeftLen = 7;
    private String textAtTitleLeft;
    private String textAtTitleRight;
    private int    textAtTitleLeftLen;

// - Main Methods - //

    // Get a list of new songs' titles, given channel and latest href
    public void setChannel( String inChannel, int inNumTries ) throws IOException
    {

        try
        {

            // Get page url and html
            channelUrl = inChannel;
            channelHtml = Jsoup.connect( channelUrl ).get().html();

            // There are 2 types of channel html's, so set title lefts/rights depending on which
            if ( channelHtml.indexOf( channelType1Check ) > 0 )
            {
                textAtTitleLeft = channelType1Check;
                textAtTitleRight = "\"}],";
                textAtTitleLeftLen = 30;
            }
            else if ( channelHtml.indexOf( channelType2Check ) > 0 )
            {
                textAtTitleLeft = channelType2Check;
                textAtTitleRight = "\"},";
                textAtTitleLeftLen = 23;
            }

            // Get channel's name
            channelName = cutOutChannelName();
            // Cut down to title/href parts so less processing
            channelHtml = cutOutMainData();

        }
        catch ( StringIndexOutOfBoundsException | IOException e )
        {

            // IF num of tries is > 0, try again
            if ( inNumTries > 0 )
            {
                setChannel( inChannel, inNumTries - 1 );
            }
            // ELSE recurse back up and throw exception
            else
            {
                throw new IOException();
            }

        }
        

    }


    // Get the dataIndex'th video's href and title
    public HashMap<String, String> getVideoData( int inIndex )
    {

        HashMap<String, String> data = new HashMap<String, String>();
        String href = null;
        int hrefPosition = 0;
        int hrefLeft;
        int hrefRight;
        String title = null;
        int titlePosition = 0;
        int titleLeft;
        int titleRight;
        int i;

        // Get to hrefIndex's position in html
        for ( i = 0; i < inIndex; i++ )
        {
            // Get index of "/watch..."
            hrefPosition = channelHtml.indexOf( textAtHrefLeft, hrefPosition ) + 1;
            // Get index of "views..."
            titlePosition = channelHtml.indexOf( textAtTitleLeft, titlePosition ) + 1;
        }

        // Get href
        hrefLeft = ( hrefPosition - 1 ) + textAtHrefLeftLen;
        hrefRight = channelHtml.indexOf( textAtHrefRight, hrefLeft );
        href = channelHtml.substring( hrefLeft, hrefRight );
        // Get title
        titleLeft = ( titlePosition - 1 ) + textAtTitleLeftLen;
        titleRight = channelHtml.indexOf( textAtTitleRight, titleLeft );
        title = channelHtml.substring( titleLeft, titleRight );
        // Insert data into HashMap and return
        data.put( "href", href );
        data.put( "title", title );

        return data;

    }


    // Get a list of new songs' titles, given channel and latest href
    public LinkedList<String> getNewSongs( String inLatestHref )
    {

        LinkedList<String> outSongs = new LinkedList<String>();
        HashMap<String, String> videoData = null;
        String href = "";
        String title = null;
        int videoIndex;

        // Get each song until reached latest checked video or reached max video index
        for ( videoIndex = 1; !href.equals( inLatestHref ) && videoIndex <= maxNumVids ; videoIndex++ )
        {

            // Get video data
            videoData = getVideoData( videoIndex );
            title = videoData.get( "title" );
            href = videoData.get( "href" );

            // IF href doesn't match latest updated href, add to songs list
            if ( !href.equals( inLatestHref ) )
            {
                outSongs.add( title );
            }

        }

        return outSongs;

    }


    // Create a subscription based on current channel
    public String createSubscription()
    {

        String outSubscription = null;
        String latestVideoHref = null;

        // Get newest video's href
        latestVideoHref = getVideoData( 1 ).get("href");

        // Put subscription string together
        outSubscription = channelName + "|--|" + channelUrl + "|--|" + latestVideoHref;

        return outSubscription;

    }


// - Accessors - //


    // Get the channel's name
    public String getChannelName()
    {
        return channelName;
    }


    // Get the page's html
    public String getHtml()
    {
        return channelHtml;
    }


// - Private Methods - //


    // Get name from html
    private String cutOutChannelName() throws IOException
    {
        String outName = null;
        int nameLeft;
        int nameRight;

        // IF html does not include required text, error in retrieving html originally, so throw exception
        if ( channelHtml.indexOf( textAtChannelNameLeft ) < 0 )
        {
            throw new IOException();
        }

        // ELSE get name and return
        nameLeft = channelHtml.indexOf( textAtChannelNameLeft ) + textAtChannelNameLeftLen;
        nameRight = channelHtml.indexOf( textAtChannelNameRight, nameLeft );
        outName = channelHtml.substring( nameLeft, nameRight );

        return outName;
    }


    // Get video titles and href's from html
    private String cutOutMainData() throws IOException, StringIndexOutOfBoundsException
    {
        String outMainData = null;

        // IF html does not include required text, error in retrieving html originally, so throw exception
        if ( channelHtml.indexOf( textAtTitleLeft ) < 0 || channelHtml.indexOf( textAtHrefLeft ) < 0 )
        {
            throw new IOException();
        }

        // ELSE get main data and return
        outMainData = channelHtml.substring( channelHtml.indexOf( textAtTitleLeft ), channelHtml.lastIndexOf( textAtHrefLeft ) + 50 );

        return outMainData;
    }

}
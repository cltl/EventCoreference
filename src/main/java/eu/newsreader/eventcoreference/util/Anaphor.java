package eu.newsreader.eventcoreference.util;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 9/9/12
 * Time: 9:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class Anaphor {


    public static boolean anaphor (String word, String lng) {
        if (lng.equalsIgnoreCase("en")) {
            return anaphorEn(word);
        }
        if (lng.equalsIgnoreCase("nl")) {
            return anaphorNl(word);
        }
        return false;
    }

    static boolean anaphorEn(String word) {
        if ((word.equalsIgnoreCase("i")) ||
                (word.equals("he")) ||
                (word.equals("she")) ||
                (word.equals("we")) ||
                (word.equals("they")) ||
                (word.equals("us")) ||
                (word.equals("them")) ||
                (word.equals("you")) ||
                (word.equals("me")) ||
                (word.equals("it")) ||
                (word.equals("that")) ||
                (word.equals("this")) ||
                (word.equals("there")) ||
                (word.equals("when")))

        {
            return true;
        }
        return false;
    }

    static boolean anaphorNl(String word) {
        if ((word.equalsIgnoreCase("ik")) ||
                (word.equals("hij")) ||
                (word.equals("zij")) ||
                (word.equals("wij")) ||
                (word.equals("jij")) ||
                (word.equals("zij")) ||
                (word.equals("jullie")) ||
                (word.equals("we")) ||
                (word.equals("het")) ||
                (word.equals("dat")) ||
                (word.equals("dit")) ||
                (word.equals("me")) ||
                (word.equals("mijn")) ||
                (word.equals("zijn")) ||
                (word.equals("hun")) ||
                (word.equals("hen")) ||
                (word.equals("mijn")) ||
                (word.equals("mijn")) ||
                (word.equals("daar")) ||
                (word.equals("hier")) ||
                (word.equals("nu")) ||
                (word.equals("dan")) ||
                (word.equals("dadelijk")) ||
                (word.equals("eerder")) ||
                (word.equals("later")) ||
                (word.equals("nu")) ||
                (word.equals("wanneer")))
        {
            return true;
        }
        return false;
    }

}

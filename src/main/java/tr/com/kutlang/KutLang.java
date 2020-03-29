package tr.com.kutlang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class KutLang {

    static boolean hadError = false;

    public static void main(String... args) throws IOException {
        if (args.length > 1) {
            System.out.println( "Usage: kutlang [script]" );
            System.exit( 64 );
        } else if (args.length == 1) {
            runFile( args[0] );
        } else {
            runPrompt();
        }
    }

    //

    /**
     * en: directly executes given source code
     * tr: kaynak kodu direkt çalıştır
     *
     * @param path
     * @throws IOException
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes( Paths.get( path ) );
        run( new String( bytes, Charset.defaultCharset() ) );
        // Indicate an error in the exit code.
        if (hadError) System.exit( 65 );
    }

    /**
     * en: spawns an interactive prompt
     * tr: interaktif komut istemi(prompt) çalıştır
     *
     * @throws IOException
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader( System.in );
        BufferedReader reader = new BufferedReader( input );

        for (; ; ) {
            System.out.print( "> " );
            run( reader.readLine() );
            hadError = false;
        }
    }

    /**
     * en: runs the source
     * tr: kaynak kodu çalıştır
     *
     * @param source
     */
    private static void run(String source) {
        Scanner scanner = new Scanner( source );
        List<Token> tokens = scanner.scanTokens();

        // For now, just print the tokens.
        for (Token token : tokens) {
            System.out.println( token );
        }
    }

    static void error(int line, String message) {
        report( line, "", message );
    }

    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message );
        hadError = true;
    }

}

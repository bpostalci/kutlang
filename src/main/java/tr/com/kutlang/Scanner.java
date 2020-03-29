package tr.com.kutlang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tr.com.kutlang.TokenType.*;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put( "and", AND );
        keywords.put( "class", CLASS );
        keywords.put( "else", ELSE );
        keywords.put( "false", FALSE );
        keywords.put( "for", FOR );
        keywords.put( "fun", FUN );
        keywords.put( "if", IF );
        keywords.put( "nil", NIL );
        keywords.put( "or", OR );
        keywords.put( "print", PRINT );
        keywords.put( "return", RETURN );
        keywords.put( "super", SUPER );
        keywords.put( "this", THIS );
        keywords.put( "true", TRUE );
        keywords.put( "var", VAR );
        keywords.put( "while", WHILE );
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add( new Token( EOF, "", null, line ) );
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken( LEFT_PAREN );
                break;
            case ')':
                addToken( RIGHT_PAREN );
                break;
            case '{':
                addToken( LEFT_BRACE );
                break;
            case '}':
                addToken( RIGHT_BRACE );
                break;
            case ',':
                addToken( COMMA );
                break;
            case '.':
                addToken( DOT );
                break;
            case '-':
                addToken( MINUS );
                break;
            case '+':
                addToken( PLUS );
                break;
            case ';':
                addToken( SEMICOLON );
                break;
            case '*':
                addToken( STAR );
                break;
            case '!':
                addToken( match( '=' ) ? BANG_EQUAL : BANG );
                break;
            case '=':
                addToken( match( '=' ) ? EQUAL_EQUAL : EQUAL );
                break;
            case '<':
                addToken( match( '=' ) ? LESS_EQUAL : LESS );
                break;
            case '>':
                addToken( match( '=' ) ? GREATER_EQUAL : GREATER );
                break;
            case '/':
                if (match( '/' )) {
                    // A comment goes until the end of the line. Ignore comments
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match( '*' )) {
                    blockComment();
                } else {
                    addToken( SLASH );
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            default:
                // handle number literals
                if (isDigit( c )) {
                    number();
                } else if (isAlpha( c )) {
                    identifier();
                } else {
                    KutLang.error( line, "Unexpected character '" + c + "'" );
                }
                break;
        }
    }

    // handle block comments
    private void blockComment() {
        // TODO: allow nesting
        while (peek() != '*' && peekNext() != '/' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (peek() != '*' || peekNext() != '/') {
            KutLang.error( line, "Unclosed block comment" );
        } else {
            // advance two more times to skip '*/'
            advance();
            advance();
        }

    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha( c ) || isDigit( c );
    }

    private void identifier() {
        while (isAlphaNumeric( peek() )) advance();

        // See if the identifier is a reserved word.
        String text = source.substring( start, current );

        TokenType type = keywords.get( text );
        addToken( type == null ? IDENTIFIER : type );
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }


    private void number() {
        while (isDigit( peek() )) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit( peekNext() )) {
            // Consume the "."
            advance();

            while (isDigit( peek() )) advance();
        }

        addToken( NUMBER,
                  Double.parseDouble( source.substring( start, current ) ) );
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        // Unterminated string.
        if (isAtEnd()) {
            KutLang.error( line, "Unterminated string." );
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring( start + 1, current - 1 );
        addToken( STRING, value );
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt( current + 1 );
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt( current );
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt( current ) != expected) return false;

        current++;
        return true;
    }

    private char advance() {
        current++;
        return source.charAt( current - 1 );
    }

    private void addToken(TokenType type) {
        addToken( type, null );
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring( start, current );
        tokens.add( new Token( type, text, literal, line ) );
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }


}

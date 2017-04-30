package ujmp_trial;

/**
 * Created by patricia on 26/03/17.
 */
public class Unicode {

    public enum Conversion {
//        Character(char),
//        String(String)
        }

    // Source of Unicode -> ASCII mappings:
    // http://lexsrv3.nlm.nih.gov/LexSysGroup/Projects/lvg/current/docs/designDoc/UDF/unicode/DefaultTables/symbolTable.html
    public char unicodeToASCII(char c) {
        switch (c) {
            case '«': return '"';
            case '´': return '\'';
            case '»': return '"';
            case '÷': return '/';
            case 'ǀ': return '|';
            case 'ǃ': return '!';
            case 'ʹ': return '\'';
            case 'ʺ': return '"';
            case 'ʼ': return '\'';
            case '˄': return '^';
            case 'ˆ': return '^';
            case 'ˈ': return '\'';
            case 'ˋ': return '`';
            case 'ˍ': return '_';
            case '˜': return '~';
            case '։': return ':';
            case '׀': return '|';
            case '׃': return ':';
            case '٪': return '%';
            case '٭': return '*';
            case '‐': return '-';
            case '‑': return '-';
            case '‒': return '-';
            case '–': return '-';
            case '—': return '-';
            case '―': return '-';
            case '‗': return '_';
            case '‘': return '\'';
            case '’': return '\'';
            case '‚': return ',';
            case '‛': return '\'';
            case '“': return '"';
            case '”': return '"';
            case '„': return '"';
            case '‟': return '"';
            case '′': return '\'';
            case '″': return '"';
            case '‵': return '`';
            case '‶': return '"';
            case '‸': return '^';
            case '‹': return '<';
            case '›': return '>';
            case '‽': return '?';
            case '⁄': return '/';
            case '⁎': return '*';
            case '⁒': return '%';
            case '⁓': return '~';
            case '−': return '-';
            case '∕': return '/';
            case '∖': return '\\';
            case '∗': return '*';
            case '∣': return '|';
            case '∶': return ':';
            case '∼': return '~';
            case '⌃': return '^';
            case '♯': return '#';
            case '✱': return '*';
            case '❘': return '|';
            case '❢': return '!';
            case '⟦': return '[';
            case '⟨': return '<';
            case '⟩': return '>';
            case '⦃': return '{';
            case '⦄': return '}';
            case '〃': return '"';
            case '〈': return '<';
            case '〉': return '>';
            case '〛': return ']';
            case '〜': return '~';
            case '〝': return '"';
            case '〞': return '"';/*
            case '‖': return "||";
            case '‴': return "'''";
            case '‷': return "'''";
            case '≤': return "<=";
            case '≥': return ">=";
            case '≦': return "<=";
            case '≧': return ">=";
            case '…': return "...";*/
        }
        return ' ';
    }

    public String unicodeToASCII(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            c = unicodeToASCII(c);
            sb.append(c);
        }
        return sb.toString();
    }
}

/*
pub enum Conversion {
    Char(char),
    String(String),
    None(char),
}

// Source of Unicode -> ASCII mappings:
// http://lexsrv3.nlm.nih.gov/LexSysGroup/Projects/lvg/current/docs/designDoc/UDF/unicode/DefaultTables/symbolTable.html
pub fn simplify_unicode_punct(c: char) -> Conversion {
    match c {
        '«' => Conversion::Char('"'),
        '´' => Conversion::Char('\''),
        '»' => Conversion::Char('"'),
        '÷' => Conversion::Char('/'),
        'ǀ' => Conversion::Char('|'),
        'ǃ' => Conversion::Char('!'),
        'ʹ' => Conversion::Char('\''),
        'ʺ' => Conversion::Char('"'),
        'ʼ' => Conversion::Char('\''),
        '˄' => Conversion::Char('^'),
        'ˆ' => Conversion::Char('^'),
        'ˈ' => Conversion::Char('\''),
        'ˋ' => Conversion::Char('`'),
        'ˍ' => Conversion::Char('_'),
        '˜' => Conversion::Char('~'),
        '։' => Conversion::Char(':'),
        '׀' => Conversion::Char('|'),
        '׃' => Conversion::Char(':'),
        '٪' => Conversion::Char('%'),
        '٭' => Conversion::Char('*'),
        '‐' => Conversion::Char('-'),
        '‑' => Conversion::Char('-'),
        '‒' => Conversion::Char('-'),
        '–' => Conversion::Char('-'),
        '—' => Conversion::Char('-'),
        '―' => Conversion::Char('-'),
        '‗' => Conversion::Char('_'),
        '‘' => Conversion::Char('\''),
        '’' => Conversion::Char('\''),
        '‚' => Conversion::Char(','),
        '‛' => Conversion::Char('\''),
        '“' => Conversion::Char('"'),
        '”' => Conversion::Char('"'),
        '„' => Conversion::Char('"'),
        '‟' => Conversion::Char('"'),
        '′' => Conversion::Char('\''),
        '″' => Conversion::Char('"'),
        '‵' => Conversion::Char('`'),
        '‶' => Conversion::Char('"'),
        '‸' => Conversion::Char('^'),
        '‹' => Conversion::Char('<'),
        '›' => Conversion::Char('>'),
        '‽' => Conversion::Char('?'),
        '⁄' => Conversion::Char('/'),
        '⁎' => Conversion::Char('*'),
        '⁒' => Conversion::Char('%'),
        '⁓' => Conversion::Char('~'),
        '−' => Conversion::Char('-'),
        '∕' => Conversion::Char('/'),
        '∖' => Conversion::Char('\\'),
        '∗' => Conversion::Char('*'),
        '∣' => Conversion::Char('|'),
        '∶' => Conversion::Char(':'),
        '∼' => Conversion::Char('~'),
        '⌃' => Conversion::Char('^'),
        '♯' => Conversion::Char('#'),
        '✱' => Conversion::Char('*'),
        '❘' => Conversion::Char('|'),
        '❢' => Conversion::Char('!'),
        '⟦' => Conversion::Char('['),
        '⟨' => Conversion::Char('<'),
        '⟩' => Conversion::Char('>'),
        '⦃' => Conversion::Char('{'),
        '⦄' => Conversion::Char('}'),
        '〃' => Conversion::Char('"'),
        '〈' => Conversion::Char('<'),
        '〉' => Conversion::Char('>'),
        '〛' => Conversion::Char(']'),
        '〜' => Conversion::Char('~'),
        '〝' => Conversion::Char('"'),
        '〞' => Conversion::Char('"'),
        '‖' => Conversion::String("||".to_string()),
        '‴' => Conversion::String("'''".to_string()),
        '‷' => Conversion::String("'''".to_string()),
        '≤' => Conversion::String("<=".to_string()),
        '≥' => Conversion::String(">=".to_string()),
        '≦' => Conversion::String("<=".to_string()),
        '≧' => Conversion::String(">=".to_string()),
        '…' => Conversion::String("...".to_string()),
        _ => Conversion::None(c),
    }
}

pub fn simplify_unicode(s: &str) -> String {
    s.chars().fold(String::with_capacity(s.len()), |mut s, c| {
        match simplify_unicode_punct(c) {
            Conversion::Char(c) => s.push(c),
            Conversion::String(ss) => s.push_str(&ss),
            Conversion::None(c) => s.push(c),
        }

        s
    })
}
 */

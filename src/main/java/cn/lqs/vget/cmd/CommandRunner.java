package cn.lqs.vget.cmd;

/**
 * parse command arguments of two types:<br>
 * 1. -key [val] <br>
 * 2. --[arg]
 */
public class CommandRunner {

    public static void main(String[] args) {
        if (args.length == 0){
            throw new IllegalArgumentException("Need to specify the URL.");
        }
        for (int i = 0; i < args.length - 1; i++) {
            if (!args[i].isEmpty()) {
                if (args[i].length() > 1 && args[i].charAt(0) == '-') {
                    if (args[i].charAt(1) == '-') {
                        // assume as type2
                    }else {
                        // assume as type1
                        i++;
                    }
                }
            }
        }
        String netResourceUrl = args[args.length - 1].trim();
    }

}

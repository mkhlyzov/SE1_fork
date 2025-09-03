import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class  Stepscount {

//    private static int sum = 0; 
//    private static int games = 0;
   public static void main(String[] args) throws IOException 
   {
        String stringpath;
        if (args.length >= 1) {
            stringpath = args[0];
        }
        
        else {
            stringpath = "logs/logs_algo";
        }

        Path path = new File(stringpath).toPath();
        List <Integer> stepslist = Files.walk(path)
            .filter(Files::isRegularFile)
            .filter(f->f.toString().endsWith(".txt"))
            .map(Stepscount::process)
            .collect(Collectors.toList());
        double average = stepslist.stream()
                         .mapToInt(Integer::intValue)
                         .average()
                         .orElse(0);
        double variance = stepslist.stream() 
                        .mapToDouble(i -> Math.pow(i - average, 2)) 
                        .average() 
                        .orElse(0);

        double stdDev = Math.sqrt(variance);
        System.out.println("Average Steps: " + average);   
        System.out.println("Variance: " + variance);
        System.out.println("Standard Deviation: " + stdDev);
   }


    private static int process(Path filepath) {
    //body of the for loop
        try {   
            long res1 = Files.lines(filepath)
            .map(String::trim)
            .filter(s->s.contains("📜 Aktuelle Spielkarte"))
            .count();
            int turnNumber = (int)res1 - 1;
            System.out.println(filepath + " " + turnNumber);
            //sum += turnNumber;
            //games++;
            return turnNumber;
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return 0;
    }

    // private static int extractTurnNumber(String line)
    // {
    //     String[] parts = line.split("\\D+");
    //     for(String part: parts) {
    //         if(!part.isEmpty())
    //         {
    //             try {
    //                 return Integer.parseInt(part);
    //             } catch (NumberFormatException ignored) {}
    //         }
    //     }
    //     return -1;
    // }
}
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
Program input is very strict on what it allows as a country
*/
public class IRoadTrip {

    public static HashMap<String, HashMap<String, Integer>> bordersData;    // Main Graph showing countries and it's boarders with its distance
    public static HashMap<String, HashMap<String, Integer>> countriesDistance= new HashMap<String, HashMap<String, Integer>>(); //Stores the distaces between contries
    public static HashMap<String, Integer> boarderingCountriesDistance = new HashMap<String, Integer>(); // Stores the distances that of the countries that are boardering
    public static HashMap<String, String> countryCodes;         // stores the country name and country ID code in hashmap
    public static HashMap<String, String> countryCases;         // stores countries that have bad data input from files

    /* Constructor takes 3 file inputs and if they are not correct then the program ends
    reads borders and make a haspmap
    reads country codes to translate
    reads capdist to find the weights
    */
    public IRoadTrip(String[] args) throws FileNotFoundException {
        if (args.length != 3) {
            System.err.println("Needs borders.txt capdist.csv state_name.tsv");
            System.exit(1);
        }

        // Initialize data structures
        bordersData = new HashMap<>();
        countryCodes = new HashMap<>();
        countryCases = new HashMap<>();
        countriesDistance = new HashMap<>();
        boarderingCountriesDistance = new HashMap<>();

        // generates the bad file inputs as cases
        addCountryCases();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("borders.txt")) {
                // checks if has borders.txt
                continue;
            } else if (args[i].equalsIgnoreCase("capdist.csv")) {
                // checks if has capdist.csv
                continue;
            } else if (args[i].equalsIgnoreCase("state_name.tsv")) {
                // checks if has state_name.tsv
                continue;
            } else {
                System.err.println("Not proper files");
                System.exit(1);
            }
        }

        // Reads capdist.csv
        readCountryDistances("capdist.csv");
        // Reads state_name.tsv
        readCountryCodes("state_name.tsv");
        // Reads borders.txt
        readBordersData("borders.txt");
    }

    // function hard codes bad input from the file so program could properly function
    public static void addCountryCases() {   
        countryCases.put("United States", "United States of America");
        countryCases.put("US", "United States of America");
        countryCases.put("Germany", "German Federal Republic");
        countryCases.put("The Central African Republic", "Central African Republic");
        countryCases.put("Cabo Verde", "Cape Verde");
        countryCases.put("Cote d'Ivoire", "Ivory Coast");
        countryCases.put("The Solomon Islands", "Solomon Islands");
        countryCases.put("Korea, North", "Korea, People's Republic of");
        countryCases.put("Korea, South", "Korea, Republic of");
        countryCases.put("Turkey (Turkiye", "Turkey (Ottoman Empire)");
        countryCases.put("Turkey", "Turkey (Ottoman Empire)");
        countryCases.put("Gambia, The", "Gambia");
        countryCases.put("Bahamas, The", "Bahamas");
        countryCases.put("Czechia", "Czech Republic");
        countryCases.put("Czechia", "Czech Republic");
        countryCases.put("North Macedonia", "Macedonia (Former Yugoslav Republic of)");
        countryCases.put("Belarus", "Belarus (Byelorussia)");
        countryCases.put("Macedonia", "Macedonia (Former Yugoslav Republic of)");
        countryCases.put("Russia", "Russia (Soviet Union)");
        countryCases.put("The Slovak Republic", "Slovakia");
        countryCases.put("Italy", "Italy/Sardinia"); 
        countryCases.put("Iran", "Iran (Persia)");
        countryCases.put("Burkina Faso", "Burkina Faso (Upper Volta)");
        countryCases.put("Denmark (Greenland", "Denmark");
        countryCases.put("Holy See (Vatican City", "Holy See (Vatican City)");
        countryCases.put("Congo, Democratic Republic of the", "Congo, Democratic Republic of (Zaire)");
        countryCases.put("Democratic Republic of the Congo", "Congo, Democratic Republic of (Zaire)");
        countryCases.put("Congo, Republic of the", "Congo");
        countryCases.put("The Republic of the Congo", "Congo");
        countryCases.put("Denmark (Greenland)", "Denmark");
        countryCases.put("Yemen", "Yemen (Arab Republic of Yemen)");
        countryCases.put("Cambodia", "Cambodia (Kampuchea)");
        countryCases.put("Tanzania", "Tanzania/Tanganyika");
        countryCases.put("Vietnam", "Vietnam, Democratic Republic of");
        countryCases.put("UK", "United Kingdom");
        countryCases.put("Timor-Leste", "East Timor");
        countryCases.put("Zimbabwe", "Zimbabwe (Rhodesia)");
        countryCases.put("Spain (Ceuta", "Spain");
        countryCases.put("Morocco (Cueta", "Morocco");
        countryCases.put("Russia (Kaliningrad", "Russia (Soviet Union)");
        countryCases.put("Russia", "Russia (Soviet Union)");
    }


    /*
    reads borders file and creates the main graph that has the counties and their boardering countries

    @param countryBorders - borders.txt file
    */
    public static void readBordersData(String countryBorders) throws FileNotFoundException {
        File file = new File(countryBorders);
        Scanner scan = new Scanner(file);

        while (scan.hasNext()) {
            String line = scan.nextLine();

            // uses regular expression pattern to read line 
            // seperateds if doensn't see the following chars
            Pattern pattern = Pattern.compile("\\b([A-Za-z\\s(),'-]+)\\b");

            // matches with the line and pattern
            Matcher matcher = pattern.matcher(line);

            // countries list to store all the country names
            List<String> countries = new ArrayList<>();

            // finds all matches and adds them to the list
            while (matcher.find()) {
                String possibleCountry = matcher.group(1).trim();

                // removes "km" and "," since useless
                if (possibleCountry.equalsIgnoreCase("km") || possibleCountry.trim().equalsIgnoreCase(",")) {
                    continue;
                }

                countries.add(matcher.group(1));
            }
            HashMap<String, Integer> borderInfo = new HashMap<>();
            String mainCountry = countries.get(0).trim();

            // if the country is in the countryCases then it changes the name to the proper format
            if(countryCases.containsKey(mainCountry)){ 
                mainCountry = countryCases.get(mainCountry);
            }

            // puts boardering countries in haspmap
            for (int i = 1; i < countries.size(); i++) {
                String borderingCountry = countries.get(i).trim();
                if (countryCases.containsKey(borderingCountry)){ // if the country is in the countryCases then it changes the name to the proper format
                    borderingCountry = countryCases.get(borderingCountry);
                }

                // gets distance between the main country and boardering country
                int distance = getDistance(mainCountry, borderingCountry);
                if (distance == -1){        // is -1 if no distance is found
                    continue;
                }
                borderInfo.put(borderingCountry, distance); // adds the borderingCountry and the distance to the hashmap
            }

            bordersData.put(mainCountry, borderInfo);       // puts info in main graph
        }
    }

    /*
    funciton reads capdist.csv file and gets the information on the distances between boardering countires

    @param countryDistances - capdist.csv file
    */
    public static void readCountryDistances(String countryDistances) throws FileNotFoundException {
        File file = new File(countryDistances);
        Scanner scan = new Scanner(file);

        // skips the infomation telling us the order of the file
        scan.nextLine();
        while (scan.hasNext()) {
            
            String line = scan.nextLine();

            // splits the line by commas
            String[] data = line.split(",");

            // gets country names of boardering countries and the distance
            String country1 = data[1].trim();
            String country2 = data[3].trim();
            int kmDist = Integer.parseInt(data[4].trim());

            // if country 1 is in countriesDistance hashmap then get the hashmap of the country, if its not in there add it
            if (countriesDistance.containsKey(country1)) {
                boarderingCountriesDistance = countriesDistance.get(country1);
            } else {                                                     
                boarderingCountriesDistance = new HashMap<String, Integer>();
                countriesDistance.put(country1, boarderingCountriesDistance);
            }
            // adds country 2 and distance it takes to get to country 1 in hashmap
            boarderingCountriesDistance.put(country2, kmDist); 
        }
    }

    
    /*
    function reads state_name.tsv and checks if the country still exists (has 2020 year),
    and if it does it stores the country name and country code and puts it in a hashmap

    @param countryCodesFIle - state_name.tsv file
    */
    public static void readCountryCodes(String countryCodesFile) throws FileNotFoundException {
        File file = new File(countryCodesFile);
        Scanner scan = new Scanner(file);

        // skips line telling us what the data is
        scan.nextLine();

        while (scan.hasNextLine()) {
            String[] line = scan.nextLine().split("\t");

            String countryCode = line[1].trim();
            String countryName = line[2].trim();
            String endDate = line[4];

            if (!endDate.contains("2020")) {
                continue;
            }

            countryCodes.put(countryName, countryCode);
        }
    }


    /*
    Function gets the distance between the two countries and if there is no distance then it returns -1
    
    @param country1 - main country
    @param country2 - boardering country
    @return the distance from main country to boardering country
     */ 
    public static int getDistance(String country1, String country2) {
         // turns country name into country ID
        String country1ID = countryCodes.get(country1);
        String country2ID = countryCodes.get(country2); 
        
        // if the countries are in the hashmap continue and get the distance
        if (country1ID != null && country2ID != null) { 
            HashMap<String, Integer> country1Distances = countriesDistance.get(country1ID);
            
            if (country1Distances != null) { 
                Integer distance = country1Distances.get(country2ID);       // sets as Integer so can hold null
                
                if (distance != null) {
                    return distance;
                }
            }
        }
        return -1; //if the distance is null, return -1 (no path found)
    }    
    
    /*
    function finds the shortest path from country 1 to country 2, and it tells the user the
    countires it goes through along with the distances
    
    @param country1 - starting country
    @param country2 - destination country
    @return List<String> - list of the counties to the shortest path
    */
    public List<String> findPath(String countryOne, String countryTwo) {
        HashMap<String, Integer> distances = new HashMap<>();
        HashMap<String, String> previousCountry = new HashMap<>();
        PriorityQueue<CountryDistance> minHeap = new PriorityQueue<>();
        List<String> travelled = new ArrayList<>();

        // checks if the countries are the same
        if (countryOne.equalsIgnoreCase(countryTwo)) {
            return travelled;
        }

        // sets the borders as postive infinity to start dikjstars
        for (String border : bordersData.keySet()) {
            distances.put(border, Integer.MAX_VALUE);
        }

        // initalizes that we start off at 0
        distances.put(countryOne, 0);
        minHeap.add(new CountryDistance(countryOne, 0));

        while (!minHeap.isEmpty()) {
            CountryDistance currentNode = minHeap.poll();
            String currentCountry = currentNode.getCountry();

            // if the country is in borders it loops though the borders to find the distances and weight between them
            if (bordersData.containsKey(currentCountry)) {
                for (Entry<String, Integer> borders : bordersData.get(currentCountry).entrySet()) {
                    String borderingCountry = borders.getKey();      // gets adjacent countries
                    int addWeight = borders.getValue() + distances.get(currentCountry); // gets the weight of the starting country and boardering countries

                    // if the new distance/weight is less than the current distance then we have to update the distance
                    if (addWeight < distances.getOrDefault(borderingCountry, Integer.MAX_VALUE)) {
                        distances.put(borderingCountry, addWeight);
                        minHeap.add(new CountryDistance(borderingCountry, addWeight));
                        previousCountry.put(borderingCountry, currentCountry);
                    }
                }
            }
        }

        // loops until country becomes the starting country
        while (!countryTwo.equalsIgnoreCase(countryOne)) {
            String prevCountry = previousCountry.get(countryTwo);

            // checks if the distances exist
            if (distances.get(countryTwo) == null || distances.get(prevCountry) == null) {
                return null;
            }

            // stores data in traveled
            travelled.add("\n" + prevCountry + " --> " + countryTwo + " (" + (distances.get(countryTwo) - distances.get(prevCountry))+ " km.)");
            countryTwo = prevCountry;
        }

        // orders it in order now
        Collections.reverse(travelled);
        return travelled;
    }
    
    /*
    class has country name and the distance of that country
    Used for Dijkstras
    */
    public static class CountryDistance implements Comparable<CountryDistance>{
        String countryNode;
        int distance;
    
        public CountryDistance(String country, int distance) {      // constructor
            this.countryNode = country;
            this.distance = distance;
        }

        public Integer getDistance() {
            return distance;
        }

        public String getCountry() {
            return countryNode;
        }

        // compares distances
        @Override
        public int compareTo(CountryDistance next) {
            return Integer.compare(this.distance, next.distance);
        }
    }


    /*
    function is called and gets users input, if input is not a county then it loops until it's either a country
    or the user enters exit. The inputs are very specfic to count as the country due to the strict files
    */
    public void acceptUserInput() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter the name of the first country (type EXIT to quit): ");
            String countryOne = scanner.nextLine().trim();

            if (countryOne.equalsIgnoreCase("EXIT")) {
                break;
            } else if (!isValidCountry(countryOne)) {
                System.out.println("Invalid country name. Please enter a valid country name.");
                continue;
            }

            System.out.print("Enter the name of the second country (type EXIT to quit): ");
            String countryTwo = scanner.nextLine().trim();

            if (countryTwo.equalsIgnoreCase("EXIT")) {
                break;
            } else if (!isValidCountry(countryTwo)) {
                System.out.println("Invalid country name. Please enter a valid country name.");
                continue;
            }

            //if the findPath == null then there is no path
            if(findPath(countryOne, countryTwo) == null){
                System.out.println("Sorry no path found between " + countryOne + " and " + countryTwo + ".");
                continue;
            }
    
            // prints results
            System.out.println("Route from " + countryOne + " to " + countryTwo + ":");
            if (findPath(countryOne, countryTwo).isEmpty()) {
                System.out.println("Shortest path from " + countryOne + " to " + countryTwo + " is 0km");
                continue;
            }
            System.out.println("Shortest path from " + countryOne + " to " + countryTwo + " is " + findPath(countryOne, countryTwo));
        }
    }


    /*
    function checks if the user input is in either the countryCases hashmap or boardersData hashmap
    
    @param input -  user input 
    @return boolean - returns true or false of if the input was a valid country
    */
    private boolean isValidCountry(String input) {
        if (countryCases.containsKey(input)) {
            input = countryCases.get(input);
        }

        if (bordersData.containsKey(input)) {
            return true;
        }

        return false;
    }

    public static void main(String[] args) throws FileNotFoundException {
        IRoadTrip roadTrip = new IRoadTrip(args);
        roadTrip.acceptUserInput();
    }
}
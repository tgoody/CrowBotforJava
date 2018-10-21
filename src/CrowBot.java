import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
//import java.util.Scanner;
//import java.util.Arrays;
//import java.util.ArrayList;
import java.util.*;
import java.lang.String;
import org.json.*;




public class CrowBot {

    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        boolean categoryBool = false;
        String categoryType = "";
        System.out.println("Enter keywords separated by commas");
        String keywords = input.nextLine();
        keywords = keywords.trim();
        ArrayList<String> commaWords = new ArrayList<String>(Arrays.asList(keywords.split(",")));
        System.out.println("Enter color separated by spaces");
        String color = input.nextLine();
        if(color.isEmpty()){
            color = "0";
        }
        ArrayList<String> spaceColor = new ArrayList<String>(Arrays.asList(color.split(" ")));


        System.out.println("Do you want to choose a specific category? Y/N");
        String categorychoice = input.nextLine();
        if(categorychoice.toUpperCase().equals("Y")){
            categoryBool = true;
            System.out.println("What category?");
            System.out.println("(Shirts, Accessories, new, bags, sweatshirts, pants, skate, hats, jackets, tops/sweaters)");
            categoryType = input.nextLine();
            categoryType = categoryType.toLowerCase();
        }

        System.out.println("What size if applicable? Please put 0 if there is no size. Put \"CHOOSE_FOR_ME\" if you want any size.");
        String sizechoice = input.nextLine();
        if(sizechoice.equals("0")){
            sizechoice = "N/A";
        }
        else{
            sizechoice = sizechoice.toUpperCase();
        }


        URL url = new URL("https://www.supremenewyork.com/mobile_stock.json");

        ArrayList<Item> matches = findMatches(url, commaWords, categoryType);

        int maxIndex = 0;

        for(int i = 0; i < matches.size(); i++){
            for(String word : commaWords){
                if (matches.get(i).name.toLowerCase().contains(word.toLowerCase())){
                    matches.get(i).count++;
                }

            }
            if(matches.get(i).count > matches.get(maxIndex).count){
                maxIndex = i;
            }

        }

        Item bestMatch = matches.get(maxIndex);



        for(int i = 0; i < matches.size(); i++){
            System.out.println(matches.get(i).name);
            System.out.println("Best match: " + bestMatch.name);
        }
        System.out.println("\n\n\n");

        Item specialItem = fillData(bestMatch, spaceColor, sizechoice);

        System.out.println("Name: " + specialItem.name);
        System.out.println("Product ID: " + specialItem.productID);
        System.out.println("Category: " + specialItem.category);
        System.out.println("Special ID: " + specialItem.specialID);
        System.out.println("Color: " + specialItem.color);
        System.out.println("Stock Level: " + specialItem.stocklevel);
        System.out.println("Size: " + specialItem.size);
        System.out.println("Color ID: " + specialItem.colorID);


    }




    public static ArrayList<Item> findMatches(URL url, ArrayList<String> keywords, String categoryType){
        InputStream inputStream;
        ArrayList<Item> matchedItems = new ArrayList<>();
        boolean goodToAdd;

        try {
            URLConnection connection = url.openConnection();
            inputStream = connection.getInputStream();
            StringBuffer allLines = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String temp;
            temp = br.readLine();
            while(temp != null){
                allLines.append(temp);
                temp = br.readLine();

            }
            br.close();

            JSONObject supremeMap = new JSONObject(allLines.toString()).getJSONObject("products_and_categories");

            Iterator searchMan = supremeMap.keys();
            while(searchMan.hasNext()){

                Object category = searchMan.next();
                if(!categoryType.isEmpty() && !(category.toString().toLowerCase().equals(categoryType))){
                    continue;
                }
                JSONArray items = supremeMap.getJSONArray((String)category);
                for(int i = 0; i < items.length(); i++){
                    JSONObject product = items.getJSONObject(i);
                    String title = product.getString("name");
                    for(int j = 0; j < keywords.size(); j++){
                        if(title.toLowerCase().contains(keywords.get(j).toLowerCase())){
                            goodToAdd = true;
                            Item foundItem = new Item();
                            foundItem.setName(product.getString("name"));
                            foundItem.setProductID(product.getInt("id"));
                            foundItem.setCategory(product.getString("category_name"));
                            if(matchedItems.isEmpty()){
                                matchedItems.add(foundItem);
                            }
                            for(int k = 0; k < matchedItems.size(); k++){
                                if(matchedItems.get(k).name.equals(foundItem.name)){
                                    goodToAdd = false;
                                    break;
                                }
                            }
                            if(goodToAdd){
                                matchedItems.add(foundItem);
                            }
                        }

                    }
                }





            }

        }
        catch(Exception whoops){
            System.out.println("Didn't work. java sucks.");
            return matchedItems;
        }


        return matchedItems;
    }

    public static Item fillData(Item bestMatchItem, ArrayList<String> colors, String sizechoice) {

        InputStream inputStream2;

        try {
                URL url2 = new URL("https://www.supremenewyork.com/shop/" + bestMatchItem.productID + ".json");
                URLConnection urlConnection2 = url2.openConnection();
                inputStream2 = urlConnection2.getInputStream();
                StringBuffer allLines = new StringBuffer();
                BufferedReader br2 = new BufferedReader(new InputStreamReader(inputStream2, "UTF-8"));
                String temp2;
                temp2 = br2.readLine();
                while(temp2 != null){
                    allLines.append(temp2);
                    temp2 = br2.readLine();

                }

                br2.close();

                JSONArray styleMap = new JSONObject(allLines.toString()).getJSONArray("styles");

                for(int j = 0; j < styleMap.length(); j++){

                    JSONObject colorProduct = styleMap.getJSONObject(j);
                    String title = colorProduct.getString("name");

                    if(colors.get(0).equals("0")){
                        colors.set(0, title);
                    }

                    for(int k = 0; k < colors.size(); k++){
                        if(title.toLowerCase().equals(colors.get(k).toLowerCase())){
                            bestMatchItem.setSpecialID(colorProduct.getInt("id"));
                            bestMatchItem.setColor(colorProduct.getString("name"));


                            JSONArray sizeMap = colorProduct.getJSONArray("sizes");

                            for(int l = 0; l < sizeMap.length(); l++) {
                                JSONObject certainSize = sizeMap.getJSONObject(l);
                                String sizeName = certainSize.getString("name");

                                if(sizechoice.equals("CHOOSE_FOR_ME")){
                                    if(certainSize.getInt("stock_level") == 1){
                                        bestMatchItem.size = sizeName;
                                        bestMatchItem.colorID = certainSize.getInt("id");
                                        bestMatchItem.stocklevel = certainSize.getInt("stock_level");
                                        return bestMatchItem;
                                    }
                                    else{continue;}
                                }

                                else if(!sizeName.toUpperCase().equals(sizechoice)){
                                    if(l == sizeMap.length()-1){
                                        int colorID = certainSize.getInt("id");
                                        int stockLevel = certainSize.getInt("stock_level");

                                        bestMatchItem.setSize(sizeName);
                                        bestMatchItem.setColorID(colorID);
                                        bestMatchItem.setStocklevel(stockLevel);
                                        return bestMatchItem;
                                    }
                                    continue;
                                }



                                int colorID = certainSize.getInt("id");
                                int stockLevel = certainSize.getInt("stock_level");

                                bestMatchItem.setSize(sizeName);
                                bestMatchItem.setColorID(colorID);
                                bestMatchItem.setStocklevel(stockLevel);
                                return bestMatchItem;
                            }
                        }
                    }

                }

        }








        catch(Exception whoops){
            System.out.println("whoops");
        }

        return bestMatchItem;
    }



}

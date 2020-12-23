import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.*;



public class Preprocess{
    
    String phrase;
    ArrayList<String> list;
    List<String> stop_words;
    List<String> dictionary;
    List<String> dictionary_index;
    List<String[]> word_vectors;
    ArrayList<String> removed_stop_words = new ArrayList<String>();
    ArrayList<String> stemmed_words = new ArrayList<String>();
    public Preprocess(String phrase){
        //removes non a-z values (numbers & punctuation)
        this.phrase = phrase.toLowerCase().replaceAll("[^a-zA-Z ]", "");
        // removes non single spaces
        this.phrase = this.phrase.replaceAll("\\s+", " ");
    }

    
    public static void main(String[] args) throws IOException{
        //Preprocess s = new Preprocess("Hello, my name is ned! and I am 76 yexxxars old. I liked running argued");
        Preprocess preprocess = new Preprocess("reward points not received");
        preprocess.tokenize();
        preprocess.removeStopWords();
        preprocess.stem();
        ArrayList<Double> phrase_embeddings = preprocess.getPhraseEmbeddings();
        preprocess.outputCsv(phrase_embeddings);
        

        /**
        System.out.println(preprocess.phrase);       
        System.out.println(preprocess.list);      
        System.out.println(preprocess.removed_stop_words);
        System.out.println(preprocess.stemmed_words);
        
        //System.out.println(Arrays.toString(s.word_vectors.get(1)));
        //System.out.println(s.word_vectors.get(1)[2]);
        **/
        
        
    }
    

    public void tokenize() throws IOException{
        //splits string on " " to make ArrayList
        this.list = new ArrayList<String>(Arrays.asList(this.phrase.split(" ")));
        this.stop_words = this.getData("stop_words.csv");
        this.dictionary = this.getData("dictionary_keys.csv");
        this.dictionary_index = this.getData("dictionary_values.csv");
        this.word_vectors = this.getWordVectors("word_vectors.csv");
  
    }
    public void removeStopWords(){
        for (int i=0; i<this.list.size(); i++){
            // does not keep words if in stop_words list or if word has more than 2 "x"s
            if (!this.stop_words.contains(this.list.get(i)) && this.countChar(this.list.get(i), 'x') < 3){
                this.removed_stop_words.add(this.list.get(i));
            }
        }
    }
   
    public List<String> getData(String path) throws IOException{
        // Reads in stop_words to List from specified csv
        int count = 0;
        List<String> content = new ArrayList<String>();
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            String word = "";
            while ((word = br.readLine()) != null) {
                content.add(word);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return content;
    }
    
    public List<String[]> getWordVectors(String path) throws IOException{
        int count = 0;
        List<String[]> content = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                content.add(line.split(","));
            }
        } catch (FileNotFoundException e) {
          //Some error logging
        }
        return content;
    }
    
    public int countChar(String str, char c){
        // used to test number of "x"s in a word
        int count = 0;

        for(int i=0; i < str.length(); i++)
        {    if(str.charAt(i) == c)
                count++;
        }

        return count;
    }
    

    public void stem(){
        //implements porter stemmer algorithm to stem the words in a list
        for (int w=0; w<this.removed_stop_words.size(); w++){
            Stemmer stemmer = new Stemmer();
            String word = removed_stop_words.get(w);
            for (int c=0; c<word.length(); c++){
                stemmer.add(word.charAt(c));
            }
            stemmer.stem();
            this.stemmed_words.add(stemmer.toString());
        }        
    }
    
    public ArrayList<Double> getPhraseEmbeddings() {
        //getPhraseEmbeddings will use a list of preprocessed words, and create a phrase embedding
        List<double[]> word_embeddings = new ArrayList<>();
        ArrayList<Double> phrase_embeddings = new ArrayList<>();

        
        //Loops through the preprocessed stemmed list of words
        for (int word=0; word<this.stemmed_words.size(); word++){
            //System.out.println(this.stemmed_words.get(word));
            try {//The try is for words that may be outside of the dictionary
                
                // Finds index of the word
                int index = this.dictionary.indexOf(this.stemmed_words.get(word));
                //System.out.println("index mapping");
                //System.out.println(index);
                // Looks up the mapping of that word to the word embeddings list
                int mapping = Integer.parseInt(this.dictionary_index.get(index));
                //System.out.println(mapping);
                // Uses the mapping to return the 100x word embedding
                String [] string_embedding = word_vectors.get(mapping);
                //System.out.println(string_embedding[0]);
                //System.out.println(string_embedding.length);
                //instantiates an float Array given that current word_embedding is string, we
                // need to cast these as floats
                double [] double_embedding = new double [string_embedding.length];
                for (int cord=0; cord<string_embedding.length; cord++){
                    double_embedding[cord] = Double.parseDouble(string_embedding[cord]);
                }
                word_embeddings.add(double_embedding);
                
            } catch (IndexOutOfBoundsException oob){ // if word is not in dictionary, then add array of all 0's
                double [] zeros = new double [100];
                for (int i=0; i<100; i++){
                    zeros[i] = 0.0;
                }
                word_embeddings.add(zeros);
            }
        }
        //System.out.println(word_embeddings.size());
        for (int cord=0; cord<100; cord++){
            double sum = 0.0;
            for (int word_embed=0; word_embed<word_embeddings.size(); word_embed++){
                sum += Double.valueOf(word_embeddings.get(word_embed)[cord]);
            }
            double avg = sum / Double.valueOf(word_embeddings.size());
            phrase_embeddings.add(avg);
        }
        //System.out.println(phrase_embeddings.size());
        //System.out.println(phrase_embeddings.get(0));
        return phrase_embeddings;
    }
    
    public void outputCsv(ArrayList<Double> phrase_embeddings) throws IOException{
        
        File file = new File("output_phrase_embeddings.csv");
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);

        for(int i=0;i<phrase_embeddings.size();i++){
            bw.write(phrase_embeddings.get(i).toString());
            bw.newLine();
        }
        bw.close();
        fw.close();
            
    }
}










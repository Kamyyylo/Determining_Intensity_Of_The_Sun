import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class IrradianceModel {
    ArrayList<Parameters> population = new ArrayList<>();
    ArrayList<Node> nodesList = new ArrayList<>();
    ArrayList<Parameters> parentsPair = new ArrayList<>();
    double crossProb=0.8, mutationProb=0.2;    //Szansa na Krzyzowanie: 0.8 , mutację: 0.1
    //czas ustawiony poki co na 2 minuty pracy algorytmu
    int workTime=10*60000;
    long timeCountStart, timeCountStop,timeWhenBestFound;
    double theBestResult,tmpBest=Double.MAX_VALUE;
    public void solution() throws FileNotFoundException
    {
        int iterator=0,iterator2=0;
        timeCountStart = System.currentTimeMillis();
        population = generatePopulation(population);
        getDataFromCSV();
        getAproxValue(population);
        sortPopulation();
        while(timeCountStop-timeCountStart<=workTime) {
            for (int i = 0; i < 100; i++) {             //stworzenie 200 dzieci zeby potem selekcją usunąć osobniki najslabsze
                makeChildren(mutationProb, crossProb);
            }
            population = selection(population);         //Tutaj wlasnie selekcja
            timeCountStop = System.currentTimeMillis();
            theBestResult = population.get(0).difference;
            if(theBestResult<tmpBest){
            timeWhenBestFound=(timeCountStop-timeCountStart)/1000;
            tmpBest=theBestResult;
               System.out.println(theBestResult);
               System.out.println(population.get(0).param);
                System.out.println("Moment znalezienia (s): "+timeWhenBestFound);
            }
            iterator++;
            if(iterator ==5)    //co 1000 nowych dzieci generuje nową populacje z 1000 losowych osobnikow żeby nie utknac wokól jednej wartosci
            {
                ArrayList<Parameters> populationTemp = new ArrayList<>();
                iterator=0;
                populationTemp = generatePopulation(populationTemp);
                getAproxValue(populationTemp);
                for(int i=0;i<1000;i++)
                {
                    population.add(populationTemp.get(i));
                }
                sortPopulation();
            }
            iterator2++;
            if(iterator2==140)
            {
                System.out.println("wpisuje najlepszy wynik do populacji aktualnej i dorabiam kompletnie nowe wartosci do niej");
                ArrayList<Parameters> escapePopulation = new ArrayList<>() ;
                escapePopulation.add(population.get(0));
                generatePopulation(escapePopulation);
                getAproxValue(escapePopulation);
                population=escapePopulation;
                iterator2=0;

            }

        }
        compareResults(population);
        System.out.println(population);
    }
    /* Geenerowanie populacji o losowych wartosciach. Parametry A,B,C,G
    to wartosci z przedziału  0-20, D z przedziału 0.50-1, E,F z przedziału 0-0.5*/
    public ArrayList<Parameters> generatePopulation(ArrayList<Parameters> newPopulation)
    {
        Random rand = new Random();
        for(int i=0; i<1000;i++){
            ArrayList<Double> param = new ArrayList<>();
            param.add(rand.nextDouble()*(2)+15);
            param.add(rand.nextDouble()*(0.1)+0);
            param.add(rand.nextDouble()*(30)+0);
            param.add(rand.nextDouble()*(2)+19);
            param.add(rand.nextDouble()*(2)+13);
            param.add(rand.nextDouble()*(2)+18);
            param.add(rand.nextDouble()*(2)+152);
            Parameters parameters = new Parameters(
                    param,0
            );
            newPopulation.add(parameters);
        }
        return newPopulation;
    }
    /*Pobieram dane z pliku csv i na ich podstawie tworzę obiekty z parametrami: dzien, godzina, natężenie w tym momencie*/
    public void getDataFromCSV() throws FileNotFoundException {
        File file = new File("data.csv");
        Scanner inputStream = new Scanner(file);
        while(inputStream.hasNext())
        {
            String data = inputStream.next();
            String[] values = data.split(",");
            Node node = new Node(
                    Integer.parseInt(values[0]),
                    Integer.parseInt(values[1]),
                    Double.parseDouble(values[2])
            );
            nodesList.add(node);
        }
    }
    /*Licze wartosc przyblizona dla podanej populacji w parametrze i obliczam kwadrat blędu wzgledem wartosci dokladnej,
    * a następnie sumuje wszystkie kwadraty błedow(około 9000) i umieszczam jako cecha danej populacji*/
    public void getAproxValue(ArrayList<Parameters> newPopulation)
    {
        double A,B,C,D,E,F,G,approxValue,preciseValue, differenceForOneNode;
        int x,y;
        for(int i=0; i<newPopulation.size();i++)
        {
           double sumOfDifferences =0;
            A= newPopulation.get(i).param.get(0);
            B= newPopulation.get(i).param.get(1);
            C= newPopulation.get(i).param.get(2);
            D= newPopulation.get(i).param.get(3);
            E= newPopulation.get(i).param.get(4);
            F= newPopulation.get(i).param.get(5);
            G= newPopulation.get(i).param.get(6);
            for(int j=0;j<nodesList.size();j++)
            {
                x= nodesList.get(j).day;
                y=nodesList.get(j).hour;
                preciseValue=nodesList.get(j).preciseValue;
                approxValue = A*Math.sin(B*x+C)*D*Math.sin(E*y+F)+G;
                differenceForOneNode = Math.pow(preciseValue-approxValue,2);
                sumOfDifferences +=differenceForOneNode;
            }
            newPopulation.get(i).setDifference(sumOfDifferences);
        }
    }

    public void sortPopulation() //Sortowanie bąbelkowe populacji
    {
        int n = population.size();
        Parameters temp ;
        for (int i=0; i<n ; i++)
        {
            for (int j=1; j<(n-i);j++)
            {
                if(population.get(j-1).difference > population.get(j).difference)
                {
                    temp = population.get(j-1);
                    population.set(j-1,population.get(j));
                    population.set(j, temp);
                }
            }
        }
    }
     public void selectParents() //Rodziców ktorzy będa generowali nowe osobniki wybieram na zasadzie turnieju
                                 //z faworyzowaniem osobnikow lepiej przystosowanych
     {
         parentsPair.clear();
         int firstClass,secondClass,thirdClass,fourthClass;
         ArrayList<Integer> tournament = new ArrayList<>();
         Random rand = new Random();
         for(int i=0;i<650;i++)
         {
             firstClass = rand.nextInt(100);  //random value 0-100
             tournament.add(firstClass);
         }
         for(int i=0;i<200;i++)
         {
             secondClass = rand.nextInt(400) +100;  //random value 100-500
             tournament.add(secondClass);
         }
         for(int i=0;i<100;i++)
         {
             thirdClass = rand.nextInt(400) +500;
             tournament.add(thirdClass);
         }
         for(int i=0;i<50;i++)
         {
             fourthClass = rand.nextInt(100) +900;
             tournament.add(fourthClass);
         }
         java.util.Collections.shuffle(tournament);
         int mother,father;
         mother = rand.nextInt(1000);
         father = rand.nextInt(1000);
         parentsPair.add(population.get(mother));
         parentsPair.add(population.get(father));
     }
        /*
        * "Robienie" dzieci z uwzlednieniem szansy na krzyzowanie danej pary rodzicow
        * i mutacji ,a nastepnie dodawanie nowych osobnikow do populacji
        * */
     public void makeChildren(double mutationProb, double crossProb){
         Random rand = new Random();
         float c = rand.nextFloat(), m = rand.nextFloat();
         ArrayList<Parameters> childrenPopulation = new ArrayList<>();
        selectParents();
         if(c<crossProb){  //jesli wartość wylosowana jest ponizej 0.8 to skrzyzuj (80% szans na krzyzowanie)
             childrenPopulation = crossParents();
             if(m<mutationProb)
             {

                 int child = rand.nextInt(2); //losowanie dziecka pierwszego lub drugiego
                 mutation(childrenPopulation, child);
             }
             countChildrenDifference(childrenPopulation);
         }

     }
     /*
     * Krzyzowanie osobnikow polega na wymieszaniu genow rodzicow aby stworzyc nowe osobniki.
     * znajduje losowo dwa miejsca ktore sa przedzialem, w którym rodzice przekazują swoje geny
     * i mieszam je ze sobą. Przyklad:
     * Matka: 1234567
     * Ojciec:7654321
     * etap pierwszy: losowanie dwoch punktow do podzialu (np, 3-6)
     * Dziecko_1: _ _ _ 4 5 6 _
     * Dziecko_2: _ _ _ 4 3 2 _
     * Następnie uzupełniam wartosci ktore nie kolidują ze soba od drugiego rodzica
     * Dziecko_1: 7 _ _ 4 5 6 1
     * Dziecko_2: 1 _ _ 4 3 2 7
     * I uzupelniam wartosci ktore nie jeszcze nie wystepuja na wolnych miejscach
      * (praktycznie to nie wystepuje gdyz liczby sa zbyt zroznicowane ale szansa jakas minimalna jest)
     * */
     public ArrayList<Parameters> crossParents()
     {
         Random rand= new Random();
         int crossPoint_1=0, crossPoint_2=0;
         ArrayList child_1 = new ArrayList();
         ArrayList child_2 = new ArrayList();
         ArrayList mother = new ArrayList();
         ArrayList father = new ArrayList();
         ArrayList<Parameters> childrenPopulation = new ArrayList<>();
         mother = parentsPair.get(0).param;
         father = parentsPair.get(1).param;
         for(int i=0;i<7;i++)
         {
             child_1.add(-1);
             child_2.add(-1);
         }
         while(crossPoint_1>crossPoint_2 || crossPoint_1==crossPoint_2)
         {
             crossPoint_1=rand.nextInt(7);
             crossPoint_2=rand.nextInt(7);
         }
        for (int i=crossPoint_1;i<crossPoint_2;i++)
        {
            child_1.set(i,mother.get(i));
            child_2.set(i,father.get(i));
        }
        for(int i=0; i<crossPoint_1;i++)
        {
                if(!child_1.contains(father.get(i)));
                    child_1.set(i,father.get(i));
                if(!child_2.contains(mother.get(i)));
                    child_2.set(i,mother.get(i));
        }
         for(int i=crossPoint_2; i<mother.size();i++)
         {
             if(!child_1.contains(father.get(i)));
             child_1.set(i,father.get(i));
             if(!child_2.contains(mother.get(i)));
             child_2.set(i,mother.get(i));
         }

         for(int i=0;i<mother.size();i++)  //Szansa ze jakas liczba sie powtorzy jest bliska zeru ale jednak jest więć ta pętla zapobiegawczo
         {
             if(child_1.get(i).equals(-1))
             {
                 for(int j=0;j<mother.size();j++)
                 {
                     if(!child_1.contains(father.get(j)))
                         child_1.set(i,father.get(j));
                 }
             }
             if(child_2.get(i).equals(-1))
             {
                 for(int j=0;j<mother.size();j++)
                 {
                     if(!child_2.contains(mother.get(j)))
                         child_2.set(i,mother.get(j));
                 }
             }
         }
         Parameters newChildren_1 = new Parameters(
                child_1,0
         );
         Parameters newChildren_2 = new Parameters(
              child_2,0
         );
         childrenPopulation.add(newChildren_1);
         childrenPopulation.add(newChildren_2);

         return childrenPopulation;
     }

     /*Licze tutaj sume kwadratu bledow dla nowych dzieci*/
     public void countChildrenDifference(ArrayList<Parameters> childrenPopulation)
    {
        double A,B,C,D,E,F,G,approxValue,preciseValue, differenceForOneNode;
        int x,y;
        for(int i=0;i<2;i++){
            double sumOfDifferences =0;
            A= childrenPopulation.get(i).param.get(0);
            B= childrenPopulation.get(i).param.get(1);
            C= childrenPopulation.get(i).param.get(2);
            D= childrenPopulation.get(i).param.get(3);
            E= childrenPopulation.get(i).param.get(4);
            F= childrenPopulation.get(i).param.get(5);
            G= childrenPopulation.get(i).param.get(6);
            for(int j=0;j<nodesList.size();j++)
            {
                x= nodesList.get(j).day;
                y=nodesList.get(j).hour;
                preciseValue=nodesList.get(j).preciseValue;
                approxValue = A*Math.sin(B*x+C)*D*Math.sin(E*y+F)+G;
                differenceForOneNode = Math.pow(preciseValue-approxValue,2);
                sumOfDifferences +=differenceForOneNode;
            }
            childrenPopulation.get(i).setDifference(sumOfDifferences);
        }
        population.add(childrenPopulation.get(0));
        population.add(childrenPopulation.get(1));
        sortPopulation();
    }
    /*Mutacja to SWAP: zamiana elementow na 2 losowo wybranych pozycacj*/
    public void mutation(ArrayList <Parameters> childrenPopulation, int child) //zamiana elementów w liscie
    {
        Random rand = new Random();
        int p=0,k=0;
        while(p==k || p>k)
        {
            p=rand.nextInt(7);
            k=rand.nextInt(7);
        }
        double tmp=childrenPopulation.get(child).param.get(p);
        childrenPopulation.get(child).param.set(p,childrenPopulation.get(child).param.get(k));
        childrenPopulation.get(child).param.set(k,tmp);
    }
    /*Selekcja polega na wybraniu 1000 najlepszy osobnikow do kolejnej reprodukcji*/
    public ArrayList<Parameters> selection(ArrayList<Parameters> population)
    {
        ArrayList<Parameters> tempList = new ArrayList<>();
        sortPopulation();
        for(int i=0;i<1000;i++)
        {
            tempList.add(population.get(i));
        }
        return tempList;
    }
    public void compareResults(ArrayList<Parameters> bestPopulation) throws  FileNotFoundException
    {
        PrintWriter print = new PrintWriter("Wyniki.txt");
        double A,B,C,D,E,F,G,approxValue,preciseValue, differenceForOneNode,sumOfdifferences=0;
        int x,y;
            A= bestPopulation.get(0).param.get(0);
            B= bestPopulation.get(0).param.get(1);
            C= bestPopulation.get(0).param.get(2);
            D= bestPopulation.get(0).param.get(3);
            E= bestPopulation.get(0).param.get(4);
            F= bestPopulation.get(0).param.get(5);
            G= bestPopulation.get(0).param.get(6);
            for(int j=0;j<nodesList.size();j++) {
                x = nodesList.get(j).day;
                y = nodesList.get(j).hour;
                preciseValue = nodesList.get(j).preciseValue;
                approxValue = A * Math.sin(B * x + C) * D * Math.sin(E * y + F) + G;
                differenceForOneNode=Math.pow(preciseValue-approxValue,2);
                sumOfdifferences = sumOfdifferences + differenceForOneNode;
               // System.out.println("i: "+j+" Wynik dokladny: "+preciseValue+"         Wynik przyblizony: "+approxValue+"       Roznica^2: "+differenceForOneNode+ "   Roznica: "+Math.sqrt(differenceForOneNode));
                print.println("i:"+j+" Wynik dokladny: "+preciseValue+"         Wynik przyblizony: "+approxValue+"       Roznica^2: "+differenceForOneNode+ "   Roznica: "+Math.sqrt(differenceForOneNode));
            }
//        System.out.println("Suma kwadratów błędów: " +sumOfdifferences);
//        System.out.println("Ilosc Wezłow: "+nodesList.size());
//        System.out.println("Sredni kwadrat błedu dla jednego elementu: "+(sumOfdifferences/nodesList.size()));
//        System.out.println("Sredni blad dla jednego elementu: "+(Math.sqrt(sumOfdifferences/nodesList.size())));

        print.println("Suma kwadratów błędów: " +sumOfdifferences);
        print.println("Ilosc Wezłow: "+nodesList.size());
        print.println("Sredni kwadrat błedu dla jednego elementu: "+(sumOfdifferences/nodesList.size()));
        print.println("Sredni blad dla jednego elementu: "+(Math.sqrt(sumOfdifferences/nodesList.size())));
        print.println(population.get(0).param);
        print.println("");
        print.println(population.get(0).param.get(0)+"*Math.sin("+population.get(0).param.get(1)+"*x+"+population.get(0).param.get(2)+")*"+population.get(0).param.get(3)+"*Math.sin("+population.get(0).param.get(4)
                +"*y+"+population.get(0).param.get(5)+")+"+population.get(0).param.get(6));
        print.println("\n Moment znalezienia (s): "+timeWhenBestFound);
        print.close();
    }
}


































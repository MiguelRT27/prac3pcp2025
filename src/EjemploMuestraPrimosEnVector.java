import java.util.concurrent.atomic.AtomicInteger;

// ===========================================================================
public class EjemploMuestraPrimosEnVector {
// ===========================================================================

  // -------------------------------------------------------------------------
  public static void main( String args[] ) {
    int     numHebras, vectOpt;
    boolean option = true;
    long    t1, t2;
    double  ts, tc, tb, td;

    // Comprobacion y extraccion de los argumentos de entrada.
    if( args.length != 2 ) {
      System.err.println( "Uso: java programa <numHebras> <vectOpt>" );
      System.exit( -1 );
    }
    try {
      numHebras = Integer.parseInt( args[ 0 ] );
      vectOpt   = Integer.parseInt( args[ 1 ] );
      if( ( numHebras <= 0 ) || ( ( vectOpt != 0 ) && ( vectOpt != 1 ) ) ){
        System.err.print( "Uso: [ java programa <numHebras> <vecOpt> ] " );
        System.err.println( "donde ( numHebras > 0 )  y ( vectOpt es 0 o 1 )" );
        System.exit( -1 );
      } else {
        option = (vectOpt == 0);
      }
    } catch( NumberFormatException ex ) {
      numHebras = -1;
      System.out.println( "ERROR: Argumentos numericos incorrectos." );
      System.exit( -1 );
    }

    //
    // Eleccion del vector de trabajo
    //
    VectorNumeros vn = new VectorNumeros (option);
    long vectorTrabajo[] = vn.vector;

    //
    // Implementacion secuencial.
    //
    System.out.println( "" );
    System.out.println( "Implementacion secuencial." );
    t1 = System.nanoTime();
    for( int i = 0; i < vectorTrabajo.length; i++ ) {
      if( esPrimo( vectorTrabajo[ i ] ) ) {
        System.out.println( "  Encontrado primo: " + vectorTrabajo[ i ] );
      }
    }
    t2 = System.nanoTime();
    ts = ( ( double ) ( t2 - t1 ) ) / 1.0e9;
    System.out.println( "Tiempo secuencial (seg.):                    " + ts );

    //
    // Implementacion paralela ciclica.
    //
    System.out.println( "" );
    System.out.println( "Implementacion paralela ciclica." );
    t1 = System.nanoTime();
    MiHebraPrimoDistCiclica vCiclica[] = new MiHebraPrimoDistCiclica[numHebras];
    for (int i = 0; i < numHebras; i++) {
      vCiclica[i] = new MiHebraPrimoDistCiclica(i, numHebras, vectorTrabajo);
      vCiclica[i].start();
    }
    for (int i = 0; i < numHebras; i++) {
      try {
        vCiclica[i].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    t2 = System.nanoTime();
    tc = ( ( double ) ( t2 - t1 ) ) / 1.0e9;
    System.out.println( "Tiempo paralela ciclica (seg.):              " + tc );
    System.out.println( "Incremento paralela ciclica:                 " + (ts/tc) );

    //
    // Implementacion paralela por bloques.
    //
    System.out.println( "" );
    System.out.println( "Implementacion paralela por bloques." );
    t1 = System.nanoTime();
    MiHebraPrimoDistPorBloques vBloques[] = new MiHebraPrimoDistPorBloques[numHebras];
    for (int i = 0; i < numHebras; i++) {
      vBloques[i] = new MiHebraPrimoDistPorBloques(i, numHebras, vectorTrabajo);
      vBloques[i].start();
    }
    for (int i = 0; i < numHebras; i++) {
      try {
        vBloques[i].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    t2 = System.nanoTime();
    tb = ( ( double ) ( t2 - t1 ) ) / 1.0e9;
    System.out.println( "Tiempo paralela por bloques (seg.):            " + tb );
    System.out.println( "Incremento paralela por bloques:               " + (ts/tb) );

    //
    // Implementacion paralela dinamica.
    //
    System.out.println( "" );
    System.out.println( "Implementacion paralela dinamica." );
    t1 = System.nanoTime();
    AtomicInteger indice = new AtomicInteger(0);
    MiHebraPrimoDistDinamica vDinamica[] = new MiHebraPrimoDistDinamica[numHebras];
    for (int i = 0; i < numHebras; i++) {
      vDinamica[i] = new MiHebraPrimoDistDinamica(i, indice, vectorTrabajo);
      vDinamica[i].start();
    }
    for (int i = 0; i < numHebras; i++) {
      try {
        vDinamica[i].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    t2 = System.nanoTime();
    td = ( ( double ) ( t2 - t1 ) ) / 1.0e9;
    System.out.println( "Tiempo paralela dinamica (seg.):             " + td );
    System.out.println( "Incremento paralela dinamica:                " + (ts/td) );
  }

  // -------------------------------------------------------------------------
  static boolean esPrimo( long num ) {
    boolean cond;
    if( num < 2 ) {
      cond = false;
    } else {
      cond = true;
      long i = 2;
      while( ( i < num )&&( cond ) ) {
        cond = ( num % i != 0 );
        i++;
      }
    }
    return( cond );
  }
}

// (E)
class MiHebraPrimoDistCiclica extends Thread {
  int idHebra;
  int numHebras;
  long[] vector;

  public MiHebraPrimoDistCiclica(int idHebra, int numHebras, long[] vector) {
    this.idHebra = idHebra;
    this.numHebras = numHebras;
    this.vector = vector;
  }

  public void run() {
    for (int i = idHebra; i < vector.length; i += numHebras) {
      if (EjemploMuestraPrimosEnVector.esPrimo(vector[i])) {
        System.out.println("  Encontrado primo: " + vector[i]);
      }
    }
  }
}

class MiHebraPrimoDistPorBloques extends Thread {
  int idHebra;
  int numHebras;
  long[] vector;

  public MiHebraPrimoDistPorBloques(int idHebra, int numHebras, long[] vector) {
    this.idHebra = idHebra;
    this.numHebras = numHebras;
    this.vector = vector;
  }

  public void run() {
    int tamVector = vector.length;
    int tamBloque = tamVector / numHebras;
    int inicio = idHebra * tamBloque;
    int fin = Math.min(tamVector,tamBloque);

    for (int i = inicio; i < fin; i++) {
      if (EjemploMuestraPrimosEnVector.esPrimo(vector[i])) {
        System.out.println("  Encontrado primo: " + vector[i]);
      }
    }
  }
}

class MiHebraPrimoDistDinamica extends Thread {
  int idHebra;
  AtomicInteger indice;
  long[] vector;

  public MiHebraPrimoDistDinamica(int idHebra, AtomicInteger indice, long[] vector) {
    this.idHebra = idHebra;
    this.indice = indice;
    this.vector = vector;
  }

  public void run() {
    int i = indice.getAndIncrement();
    while (i < vector.length) {
      if (EjemploMuestraPrimosEnVector.esPrimo(vector[i])) {
        System.out.println("  Encontrado primo: " + vector[i]);
      }
      i = indice.getAndIncrement();
    }
  }
}

// ===========================================================================
class VectorNumeros {
  // ===========================================================================
  long    vector[];
  // -------------------------------------------------------------------------
  public VectorNumeros (boolean caso) {
    if (caso) {
      vector = new long [] {
              200000081L, 200000083L, 200000089L, 200000093L,
              200000107L, 200000117L, 200000123L, 200000131L,
              200000161L, 200000183L, 200000201L, 200000209L,
              200000221L, 200000237L, 200000239L, 200000243L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L
      };
    } else {
      vector = new long [] {
              200000081L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000083L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000089L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000093L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000107L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000117L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000123L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000131L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000161L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000183L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000201L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000209L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000221L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000237L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000239L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L,
              200000243L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L
      };
    }
  }
}

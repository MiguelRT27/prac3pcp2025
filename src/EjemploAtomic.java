import java.util.concurrent.atomic.AtomicInteger;

// ============================================================================
class MiHebraAtomic extends Thread {
    // ============================================================================
    int           numIters;
    AtomicInteger c;

    // --------------------------------------------------------------------------
    public MiHebraAtomic(int numIters, AtomicInteger c ) {
        this.numIters = numIters;
        this.c        = c;
    }

    // --------------------------------------------------------------------------
    public void run() {
        for( int i = 0; i < numIters; i++ ) {
            c.incrementAndGet();
        }
    }
}

// ============================================================================
class EjemploAtomic {
// ============================================================================

    // --------------------------------------------------------------------------
    public static void main( String args[] ) {
        long    t1, t2;
        double  tt;
        int     numHebras, numIters;

        // Comprobacion y extraccion de los argumentos de entrada.
        if( args.length != 2 ) {
            System.err.println( "Uso: java programa <numHebras> <numIters>" );
            System.exit( -1 );
        }
        try {
            numHebras = Integer.parseInt( args[ 0 ] );
            numIters  = Integer.parseInt( args[ 1 ] );
            if( ( numHebras <= 0 ) || ( numIters <= 0 ) ) {
                System.err.print( "Uso: [ java programa <numHebras> <n> ] " );
                System.err.println( "donde ( numHebras > 0 )  y ( numIters > 0 )" );
                System.exit( -1 );
            }
        } catch( NumberFormatException ex ) {
            numHebras = -1;
            numIters  = -1;
            System.out.println( "ERROR: Argumentos numericos incorrectos." );
            System.exit( -1 );
        }

        System.out.println( "numHebras: " + numHebras );
        System.out.println( "numIters : " + numIters );

        System.out.println( "Creando y arrancando " + numHebras + " hebras." );
        t1 = System.nanoTime();
        MiHebraAtomic v[] = new MiHebraAtomic[ numHebras ];
        AtomicInteger c = new AtomicInteger( 0 );
        for( int i = 0; i < numHebras; i++ ) {
            v[ i ] = new MiHebraAtomic( numIters, c );
            v[ i ].start();
        }
        for( int i = 0; i < numHebras; i++ ) {
            try {
                v[ i ].join();
            } catch( InterruptedException ex ) {
                ex.printStackTrace();
            }
        }
        t2 = System.nanoTime();
        tt = ( ( double ) ( t2 - t1 ) ) / 1.0e9;
        System.out.println( "Total de incrementos: " + c.get() );
        System.out.println( "Tiempo transcurrido en segs.: " + tt );
    }
}
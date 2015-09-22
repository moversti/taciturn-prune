/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ohtu.verkkokauppa;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author arvy
 */
public class KauppaTest {

    public KauppaTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    Kauppa k;
    Pankki pankki;
    Varasto varasto;
    Viitegeneraattori viite;
    
    @Before
    public void setUp() {
        pankki = mock(Pankki.class);
        varasto = mock(Varasto.class);
        viite= mock(Viitegeneraattori.class);
        k=new Kauppa(varasto, pankki, viite);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
        // luodaan ensin mock-oliot

        // määritellään että viitegeneraattori palauttaa viitten 42
        when(viite.uusi()).thenReturn(42);

        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // sitten testattava kauppa 

        // tehdään ostokset
        k.aloitaAsiointi();
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        k.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), anyString(), eq(5));
        // toistaiseksi ei välitetty kutsussa käytetyistä parametreista
    }
    
    /*
    aloitetaan asiointi, 
    koriin lisätään kaksi eri tuotetta, joita varastossa on 
    ja suoritetaan ostos. 
    varmistettava että kutsutaan pankin metodia tilisiirto 
    oikealla asiakkaalla, tilinumerolla ja summalla
    */
    @Test
    public void ostetaanKaksiEri(){
        when(viite.uusi()).thenReturn(135);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.saldo(2)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1,"suklaa",2));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2,"cola",1));
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("kake", "k4k3-42");
        verify(pankki).tilisiirto(eq("kake"), eq(135), eq("k4k3-42"), anyString(), eq(3));
    }
    
    @Test
    public void ostetaanKaksiSama(){
        when(viite.uusi()).thenReturn(135);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1,"suklaa",2));
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(1);
        k.tilimaksu("kake", "k4k3-42");
        verify(pankki).tilisiirto(eq("kake"), eq(135), eq("k4k3-42"), anyString(), eq(4));
    }
    
    @Test
    public void ostetaanKaksiToinenLoppu(){
        when(viite.uusi()).thenReturn(135);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.saldo(2)).thenReturn(0);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1,"suklaa",2));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2,"cola",1));
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("kake", "k4k3-42");
        verify(pankki).tilisiirto(eq("kake"), eq(135), eq("k4k3-42"), anyString(), eq(2));
    }
    
    @Test
    public void aloitaAsiointiResettaa(){
        when(viite.uusi()).thenReturn(135);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.saldo(2)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1,"suklaa",2));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2,"cola",1));
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.aloitaAsiointi();
        k.lisaaKoriin(2);
        k.tilimaksu("kake", "k4k3-42");
        verify(pankki).tilisiirto(eq("kake"), eq(135), eq("k4k3-42"), anyString(), eq(1));
    }
    
    @Test
    public void uusiViitenroJokaiselleMaksulle(){
        when(viite.uusi())
                .thenReturn(135)
                .thenReturn(246)
                .thenReturn(999);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.saldo(2)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1,"suklaa",2));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2,"cola",1));
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("kake", "k4k3-42");
        verify(viite,times(1)).uusi();
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("kake", "k4k3-42");
        verify(viite,times(2)).uusi();
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("kake", "k4k3-42");
        verify(viite,times(3)).uusi();
        
    }
    
    @Test
    public void koristaPoistoPalauttaaVarastoon(){
        when(viite.uusi()).thenReturn(135);
        when(varasto.saldo(1)).thenReturn(10);
        Tuote t = new Tuote(1,"suklaa",2);
        when(varasto.haeTuote(1)).thenReturn(t);
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.poistaKorista(1);
        verify(varasto).palautaVarastoon(eq(t));
    }
    
    
}

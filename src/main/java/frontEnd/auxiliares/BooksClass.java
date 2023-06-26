package frontEnd.auxiliares;
public class BooksClass{

	public enum Books {
		 B1("Adler,_Elizabeth__1991_._La_esmeralda_de_los_Ivanoff_[10057].txt",
            "A. Elizabeth. 'La esmeralda de los Ivanoff'. 1991.","1"),
		 B2("Adler_Olsen,_Jussi__1997_._La_casa_del_alfabeto_[7745].txt",
            "A.O. Jussi. 'La casa del alfabeto.' 1997.","2"),
		 B3("Aguilera,_Juan_Miguel__1998_._La_locura_de_Dios_[5644].txt",
            "A. Juan Miguel. 'La locura de Dios'. 1998.","3"),
		 B4("Alameddine,_Rabih__2008_._El_contador_de_historias_[5735].txt",
            "A. Rabih. 'El contador de historias'. 2008.","4"),
		 B5("Albom,_Mitch__2002_._Martes_con_mi_viejo_profesor_[382].txt",
            "A. Mitch. 'Martes con mi viejo profesor'. 2002.","5"),
		 B6("Alcott,_Louisa_May__1868_._Mujercitas_[11086].txt",
            "A. Louisa May. 'Mujercitas'. 1868.","6"),
		 B7("Alcott,_Louisa_May__1871_._Hombrecitos_[15392].txt",
            "A. Louisa May. 'Hombrecitos'. 1871.","7"),
		 B8("Alders,_Hanny__1987_._El_tesoro_de_los_templarios_[13014].txt",
            "A. Hanny. 'El tesoro de los templarios'. 1987.","8"),
		 B9("Alexander,_Caroline__1998_._Atrapados_en_el_hielo_[15727].txt",
            "A. Caroline. 'Atrapados en el hielo'. 1998.","9"),
		 B10("Allende,_Isabel__1982_._La_casa_de_los_espíritus_[563].txt",
            "A. Isabel. 'La casa de los espíritus'. 1982.","10"),
		 B11("Allende,_Isabel__1984_._De_amor_y_de_sombra_[6283].txt",
            "A. Isabel. 'De amor y de sombra'. 1984.","11"),
		 B12("Alten,_Steve__2001_.__Trilogía_maya_01__El_testamento_maya_[8901].txt",
            "A. Steve. 'Trilogía maya 01  El testamento maya'. 2001.","12"),
		 B13("Alten,_Steve__2008_._Al_borde_del_infierno_[12141].txt",
            "A. Steve. 'Al borde del infierno'. 2008.","13"),
		 B14("Amis,_Martin__1990_._Los_monstruos_de_Einstein_[8080].txt",
            "A. Martin. 'Los monstruos de Einstein'. 1990.","14"),
		 B15("Anderson,_Sienna__2008_._No_me_olvides_[15047].txt",
            "A. Sienna. 'No me olvides'. 2008.","15"),
		 B16("Anónimo__1554_._Lazarillo_de_Tormes_[11043].txt",
            "Anónimo. 'Lazarillo de Tormes'. 1554.","16"),
		 B17("Anónimo__2004_._Robin_Hood_[11853].txt",
            "Anónimo. 'Robin Hood'. 2004.","17"),
		 B18("Archer,_Jeffrey__1979_._Kane_y_Abel_[1965].txt",
            "A. Jeffrey. 'Kane y Abel'. 1979.","18"),
		 B19("Asimov,_Isaac__1950_._Yo,_robot_[10874].txt",
            "A. Isaac. 'Yo, robot'. 1950.","19"),
		 B20("Asimov,_Isaac__1967_._Guía_de_la_Biblia__Antiguo_Testamento__[6134].txt",
            "A. Isaac. 'Guía de la Biblia  Antiguo Testamento'. 1967.","20"),
		 B21("Asimov,_Isaac__1985_._El_monstruo_subatómico_[167].txt",
            "A. Isaac. 'El monstruo subatómico'. 1985.","21"),
		 B22("Bach,_Richard__1970_._Juan_Salvador_Gaviota_[15399].txt",
            "B. Richard. 'Juan Salvador Gaviota'. 1970.","22"),
		 B23("Baum,_Lyman_Frank__1900_._El_Mago_de_Oz_[15715].txt",
             "B. Lyman Frank. 'El Mago de Oz'. 1900.","23"),
		 B24("Beevor,_Antony__1998_._Stalingrado_[10491].txt",
            "B. Antony. 'Stalingrado'. 1998.","24"),
		 B25("Benítez,_J._J.__1984_.__Caballo_de_Troya_01__Jerusalén_[4826].txt",
            "Benítez, J. J. 'Caballo de Troya 01  Jerusalén'. 1984.","25"),
		 B26("Dickens,_Charles__1843_._Cuento_de_Navidad_[3285].txt",
            "D. Charles. 'Cuento de Navidad'. 1843.","26"),
		 B27("Dostoievski,_Fiódor__1865_._Crimen_y_castigo_[13400].txt",
            "D. Fiódor. 'Crimen y castigo'. 1865.","27"),
		 B28("Ende,_Michael__1973_._Momo_[1894].txt",
            "E. Michael. 'Momo'. 1973.","28"),
		 B29("Esquivel,_Laura__1989_._Como_agua_para_chocolate_[7750].txt",
            "E. Laura. 'Como agua para chocolate'. 1989.","29"),
		 B30("Flaubert,_Gustave__1857_._Madame_Bovary_[3067].txt",
            "F. Gustave. 'Madame Bovary'. 1857.","30"),
		 B31("Fromm,_Erich__1947_._El_miedo_a_la_libertad_[11619].txt",
            "F. Erich. 'El miedo a la libertad'. 1947.","31"),
		 B32("Gaarder,_Jostein__1991_._El_mundo_de_Sofía_[6571].txt",
            "G. Jostein. 'El mundo de Sofía'. 1991.","32"),
		 B33("Gaiman,_Neil__2002_._Coraline_[1976].txt",
            "G. Neil. 'Coraline'. 2002.","33"),
		 B34("García_Márquez,_Gabriel__1967_._Cien_años_de_soledad_[8376].txt",
            "G. M. Gabriel. 'Cien años de soledad'. 1967.","34"),
		 B35("García_Márquez,_Gabriel__1985_._El_amor_en_los_tiempos_del_cólera_[874].txt",
            "G. M. Gabriel. 'El amor en los tiempos del cólera'. 1985.","35"),
		 B36("García_Márquez,_Gabriel__1989_._El_general_en_su_laberinto_[875].txt",
            "G. M. Gabriel. 'El general en su laberinto'. 1989.","36"),
		 B37("Golding,_William__1954_._El_señor_de_las_moscas_[6260].txt",
            "G. William. 'El señor de las moscas'. 1954.","37"),
		 B38("Goleman,_Daniel__1995_._Inteligencia_emocional_[4998].txt",
            "G. Daniel. 'Inteligencia emocional'. 1995.","38"),
		 B39("Gorki,_Máximo__1907_._La_madre_[1592].txt",
            "G. Máximo. 'La madre'. 1907.","39"),
		 B40("Harris,_Thomas__1988_._El_silencio_de_los_inocentes_[11274].txt",
            "H. Thomas. 'El silencio de los inocentes'. 1988.","40"),
		 B41("Hawking,_Stephen__1988_._Historia_del_tiempo_[8536].txt",
            "H. Stephen. 'Historia del tiempo'. 1988.","41"),
		 B42("Hemingway,_Ernest__1952_._El_viejo_y_el_mar_[1519].txt",
            "H. Ernest. 'El viejo y el mar'. 1952.","42"),
		 B43("Hesse,_Herman__1919_._Demian_[2612].txt",
            "H. Herman. 'Demian'. 1919.","43"),
		 B44("Hitler,_Adolf__1935_._Mi_lucha_[11690].txt",
            "H. Adolf. 'Mi lucha'. 1935.","44"),
		 B45("Hobbes,_Thomas__1651_._Leviatán_[2938].txt",
            "H. Thomas. 'Leviatán'. 1651.","45"),
		 B46("Huxley,_Aldous__1932_._Un_mundo_feliz_[293].txt",
            "H. Aldous. 'Un mundo feliz'. 1932.","46")
;

		 
        private String nameFile;
        private String citeFile;
        private String Description;
        
        Books(String nameFile, String citeFile, String Description) {
            this.nameFile = nameFile;
            this.citeFile = citeFile;
            this.Description = Description;
        }
        
        public String getNameFile() {
            return nameFile;
        }
        
        public String getCiteFile() {
            return citeFile;
        }

        public String getDescription(){
            return Description;
        }

        public static String getInformation(String string){
            String s = "";
            for(Books book : Books.values()){
                if(string.equals(book.getNameFile())){
                    s+= book.getCiteFile()+" "+book.getDescription();
                    break;
                }
            }
            return s;
        }
    }
}
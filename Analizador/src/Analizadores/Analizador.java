package Analizadores;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class Analizador extends javax.swing.JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private NumeroLinea nm; // clase para número de línea
	private JFileChooser jfc;
	private FileNameExtensionFilter filtro = new FileNameExtensionFilter("archivos con extensión .fercho", "fercho");
	private String ruta = "", resultadoLex = "", resultadoError = "";
	private FileOutputStream output; // flujo de salida de datos
	private FileInputStream input; // flujo de entrada de datos
	private DefaultTableModel modeloSintactico, modeloSimbolos; // modelos para las JTable
	private Vector<Object> columnasSintactico, columnasSimbolos; // vectores para el JTable del análisis sintáctico
	private Vector<Object> renglonesSintactico, renglonesSimbolos; // vectores para el JTable de la tabla de símbolos
	private LinkedList<String> pila = new LinkedList<String>(); // lista ligada que funge como pila del análisis sintáctico
	private LinkedList<String> simbolosTabla = new LinkedList<String>(); // lista ligada para la tabla de símbolos
	private int numeroLinea = 0; // contador número de línea
	boolean ban = true;
	String entradas[] = { "id", "int", "float", "char", ",", ";", "+", "-", "*", "/", "(", ")", "=", "num", "$", "P", "TIPO", "V", "A", "EXP", "E", "TERM", "T", "F" };
	String estados[] = { "I0", "I1", "I2", "I3", "I4", "I5", "I6", "I7", "I8", "I9", "I10", "I11", "I12", "I13", "I14", "I15", "I16", "I17", "I18", "I19", "I20", "I21", "I22", "I23", "I24", "I25", "I26", "I27", "I28", "I29", "I30", "I31", "I32", "I33", "I34", "I35", "I36", "I37", "I38" };
	String tabla[][] = new String[39][24];
	String producciones[] = new String[20];

	public Analizador()
	{
		initComponents();
		nm = new NumeroLinea(areaFuente);
		jScrollPaneFuente.setRowHeaderView(nm);
		areaFuente.setEnabled(false);
		bGuardar.setEnabled(false);
		bGuardarComo.setEnabled(false);
		bCerrar.setEnabled(false);
		menuGuardar.setEnabled(false);
		menuCerrar.setEnabled(false);
		bAnalizar.setEnabled(false);
		jTabbedPaneAnalizado.setEnabled(false);
		areaLexico.setEnabled(false);
		areaErrores.setEnabled(false);
		areaSemantico.setEnabled(false);
		setLocationRelativeTo(this);
		setResizable(false);
		bNuevo.addActionListener(this);
		bAbrir.addActionListener(this);
		bGuardarComo.addActionListener(this);
		bGuardar.addActionListener(this);
		bCerrar.addActionListener(this);
		bAnalizar.addActionListener(this);
		itemNuevo.addActionListener(this);
		itemAbrir.addActionListener(this);
		itemGuardarComo.addActionListener(this);
		itemGuardar.addActionListener(this);
		itemCerrar.addActionListener(this);
		columnasSintactico = new Vector<Object>();
		renglonesSintactico = new Vector<Object>();
		columnasSintactico.add("Entrada");
		columnasSintactico.add("Pila");
		columnasSintactico.add("Acción");
		modeloSintactico = new DefaultTableModel((Vector) renglonesSintactico, (Vector) columnasSintactico);
		tablaSintactico.setModel(modeloSintactico);
		columnasSimbolos = new Vector<Object>();
		renglonesSimbolos = new Vector<Object>();
		columnasSimbolos.add("Componente");
		columnasSimbolos.add("Nombre");
		columnasSimbolos.add("Tipo");
		columnasSimbolos.add("Valor");
		columnasSimbolos.add("Referencia");
		modeloSimbolos = new DefaultTableModel((Vector) renglonesSimbolos, (Vector) columnasSimbolos);
		tablaSimbolos.setModel(modeloSimbolos);
		this.rellenarTabla();
		this.rellenarProducciones();
	}

	private void Abrir()
	{
		try
		{
			jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setFileFilter(filtro);
			jfc.showOpenDialog(this);
			ruta = jfc.getSelectedFile().toString();
			try
			{
				output = new FileOutputStream(ruta, true);
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "Error en el archivo", "ERROR", JOptionPane.ERROR_MESSAGE);
			}
			String contenido = "";
			byte datos[] = new byte[30];
			int leidos = 0;
			try
			{
				input = new FileInputStream(ruta);
				do
				{
					leidos = input.read(datos);
					if (leidos != -1)
						contenido += new String(datos, 0, leidos);
				}
				while (leidos != -1);
				input.close();
				output.close();
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "Error en el archivo", "ERROR", JOptionPane.ERROR_MESSAGE);
			}
			areaFuente.setText(contenido);
			areaFuente.setEnabled(true);
			bNuevo.setEnabled(false);
			bAbrir.setEnabled(false);
			bGuardar.setEnabled(true);
			bGuardarComo.setEnabled(true);
			bCerrar.setEnabled(true);
			bAnalizar.setEnabled(true);
			menuArchivo.setEnabled(false);
			menuGuardar.setEnabled(true);
			menuCerrar.setEnabled(true);
			bAnalizar.setEnabled(true);
			jTabbedPaneAnalizado.setEnabled(true);
		}
		catch (NullPointerException e)
		{
			JOptionPane.showMessageDialog(null, "Error al abrir el archivo", "ERROR", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void GuardarComo()
	{
		try
		{
			String contenido = areaFuente.getText();
			jfc = new JFileChooser();
			jfc.setSelectedFile(new File("programa.fercho"));
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setFileFilter(filtro);
			int opcion = jfc.showSaveDialog(this);
			ruta = jfc.getSelectedFile().toString();
			File archivo = jfc.getSelectedFile();
			if (opcion == JFileChooser.APPROVE_OPTION)
			{
				if (archivo.exists())
				{
					int resultado = JOptionPane.showConfirmDialog(this, "Ya existe un archivo con el mismo nombre ¿Desea sobrescribirlo?", "Archivo ya existe", JOptionPane.YES_NO_OPTION);
					if (resultado == JOptionPane.YES_OPTION)
						try
						{
							output = new FileOutputStream(ruta);
							output.write(contenido.getBytes());
							output.close();
						}
						catch (IOException e)
						{
							JOptionPane.showMessageDialog(null, "Error en el archivo", "ERROR", JOptionPane.ERROR_MESSAGE);
						}
					else
						this.GuardarComo();
				}
				else
					try
					{
						output = new FileOutputStream(ruta);
						output.write(contenido.getBytes());
						output.close();
					}
					catch (IOException e)
					{
						JOptionPane.showMessageDialog(null, "Error en el archivo", "ERROR", JOptionPane.ERROR_MESSAGE);
					}
			}
		}
		catch (NullPointerException e)
		{
			JOptionPane.showMessageDialog(null, "Error al guardar el archivo", "ERROR", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void Nuevo()
	{
		areaFuente.setEnabled(true);
		areaFuente.setText("");
		bNuevo.setEnabled(false);
		bAbrir.setEnabled(false);
		bGuardar.setEnabled(true);
		bGuardarComo.setEnabled(true);
		bCerrar.setEnabled(true);
		menuArchivo.setEnabled(false);
		menuGuardar.setEnabled(true);
		menuCerrar.setEnabled(true);
		bAnalizar.setEnabled(true);
		jTabbedPaneAnalizado.setEnabled(true);
	}

	private void Guardar()
	{
		try
		{
			String contenido = areaFuente.getText();
			if (ruta.isEmpty())
				this.GuardarComo();
			try
			{
				output = new FileOutputStream(ruta);
				output.write(contenido.getBytes());
				output.close();
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "Error en el archivo", "ERROR", JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (NullPointerException e)
		{
			JOptionPane.showMessageDialog(null, "Error al guardar el archivo", "ERROR", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void Cerrar()
	{
		int resultado = JOptionPane.showConfirmDialog(this, "¿Desea guardar antes de cerrar el archivo?", "Aviso", JOptionPane.YES_NO_OPTION);
		if (resultado == JOptionPane.YES_OPTION)
			if (ruta.isEmpty())
				this.GuardarComo();
			else
				this.Guardar();
		areaFuente.setEnabled(false);
		areaFuente.setText("");
		bNuevo.setEnabled(true);
		bAbrir.setEnabled(true);
		bGuardar.setEnabled(false);
		bGuardarComo.setEnabled(false);
		bCerrar.setEnabled(false);
		menuArchivo.setEnabled(true);
		menuGuardar.setEnabled(false);
		menuCerrar.setEnabled(false);
		bAnalizar.setEnabled(false);
		jTabbedPaneAnalizado.setEnabled(false);
		ruta = "";
		areaLexico.setText("");
		areaErrores.setText("");
		areaSemantico.setText("");
		modeloSintactico.setRowCount(0);
		modeloSimbolos.setRowCount(0);
	}

	private void analisisLexico()
	{
		String componente = "", tipo = "";
		numeroLinea = 0;
		boolean ban = true;
		try
		{
			File temporal = new File("temporal.fercho");
			output = new FileOutputStream(temporal);
			output.write(areaFuente.getText().getBytes());
			output.close();
			Reader lector = new BufferedReader(new FileReader(temporal));
			Lexer lexer = new Lexer(lector);
			numeroLinea = 1;
			String lineas[] = areaFuente.getText().split("\n");
			resultadoLex += numeroLinea + ". " + lineas[numeroLinea - 1] + "\n";
			simbolosTabla.clear();
			pila.clear(); // se limpia la pila de análisis sintáctico
			pila.push("$"); // se inserta el $ que nos dice que es el principio de la pila
			pila.push(estados[0]); // se inserta el primer elemento de nuestra tabla de análisis sintáctico, en este caso es "I0"
			while (this.ban)
			{
				renglonesSimbolos = new Vector<Object>();
				Tokens tokens = lexer.yylex();
				if (tokens == null)
				{
					lector.close();
					temporal.delete();
					break;
				}
				switch (tokens)
				{
					case id:
						componente = "id";
						resultadoLex += "\t" + lexer.Lexeme + " -> " + componente + "\n";
						if (simbolosTabla.indexOf(lexer.Lexeme) == -1 && !tipo.isEmpty())
						{
							simbolosTabla.add(lexer.Lexeme);
							renglonesSimbolos.add(componente);
							renglonesSimbolos.add(lexer.Lexeme);
							renglonesSimbolos.add(tipo);
							modeloSimbolos.addRow(renglonesSimbolos);
						}
						else if (simbolosTabla.indexOf(lexer.Lexeme) == -1)
						{
							resultadoError += "Error Semántico en la línea: " + numeroLinea + ".\nEl id: " + lexer.Lexeme + " no ha sido declarado.";
							areaLexico.setText(resultadoLex);
							JOptionPane.showMessageDialog(null, "Cadena Rechazada\nRevise pestaña de Errores");
							return;
						}
						else if (simbolosTabla.indexOf(lexer.Lexeme) != -1 && !tipo.isEmpty())
						{
							resultadoError += "Error Semántico en la línea: " + numeroLinea + ".\nEl id: " + lexer.Lexeme + " ya había sido declarado previamente.";
							areaLexico.setText(resultadoLex);
							JOptionPane.showMessageDialog(null, "Cadena Rechazada\nRevise pestaña de Errores");
							return;
						}
						ban = true;
						break;
					case num:
						resultadoLex += "\t" + lexer.Lexeme + " -> num\n";
						componente = "num";
						ban = true;
						break;
					case puntuacion:
						resultadoLex += "\t" + lexer.Lexeme + " -> " + lexer.Lexeme + "\n";
						componente = lexer.Lexeme;
						if (componente.equals(";"))
							tipo = "";
						ban = true;
						break;
					case agrupacion:
						resultadoLex += "\t" + lexer.Lexeme + " -> " + lexer.Lexeme + "\n";
						componente = lexer.Lexeme;
						ban = true;
						break;
					case operadorAritmetico:
						resultadoLex += "\t" + lexer.Lexeme + " -> " + lexer.Lexeme + "\n";
						componente = lexer.Lexeme;
						ban = true;
						break;
					case operadorAsignacion:
						resultadoLex += "\t" + lexer.Lexeme + " -> " + lexer.Lexeme + "\n";
						componente = lexer.Lexeme;
						ban = true;
						break;
					case tipo:
						resultadoLex += "\t" + lexer.Lexeme + " -> " + lexer.Lexeme + "\n";
						componente = lexer.Lexeme;
						ban = true;
						tipo = componente;
						break;
					case salto:
						numeroLinea++;
						resultadoLex += numeroLinea + ". " + lineas[numeroLinea - 1] + "\n";
						ban = false;
						break;
					case error:
						resultadoLex += "\t" + lexer.Lexeme + " -> Error Léxico\n";
						resultadoError += "Error Léxico en la línea: " + numeroLinea + ".\nEl token: " + lexer.Lexeme + " no existe.";
						areaLexico.setText(resultadoLex);
						JOptionPane.showMessageDialog(null, "Cadena Rechazada\nRevise pestaña de Errores");
						return;
				}
				areaLexico.setText(resultadoLex);
				if (ban)
					this.analisisSintactico(componente);
			}
			areaLexico.setText(resultadoLex);
			this.analisisSintactico("$"); //manda el terminador de cadena
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Error en el archivo", "ERROR", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void analisisSintactico(String componente)
	{
		if (ban)
		{
			String estado = "", entrada = "", pilaTxt = "", accionTxt = "", produccionTxt = "", estadoP = "", entradaP = "", accion = "", produccionAux[];
			int posEstado = 0, posEntrada = 0;
			boolean concuerda = false;
			entrada = componente;
			estado = pila.peek();
			while (!concuerda)
			{
				System.out.println("----");
				pilaTxt = "";
				produccionTxt = "";
				renglonesSintactico = new Vector<Object>();
				renglonesSintactico.add(entrada);
				for (int i = pila.size() - 1; i > -1; i--)
					pilaTxt += pila.get(i);
				renglonesSintactico.add(pilaTxt);
				System.out.println("Pila: " + pilaTxt);
				for (int i = 0; i < estados.length; i++)
					if (estados[i].equals(estado))
					{
						posEstado = i;
						break;
					}
				System.out.println("Estado Actual: " + estado + "\tPosición estado actual en la tabla: " + posEstado);
				for (int i = 0; i < entradas.length; i++)
					if (entradas[i].equals(entrada))
					{
						posEntrada = i;
						break;
					}
				System.out
						.println("Entrada actual: " + entrada + "\tPosición entrada actual en la tabla: " + posEntrada);
				accion = tabla[posEstado][posEntrada];
				if (accion == null)
				{
					ban = false;
					concuerda = true;
					this.Errores(pila.peek(), entrada);
					accionTxt = "Cadena Rechazada (Estado Nulo)";
					System.out.println("Resultado de la tabla (acción): " + accionTxt);
					renglonesSintactico.add(accionTxt);
					renglonesSintactico.add("Cadena Rechazada");
					modeloSintactico.addRow(renglonesSintactico);
					JOptionPane.showMessageDialog(null, "Cadena Rechazada\nRevise pestaña de Errores");
				}
				else if (accion.charAt(0) == 'I')
				{
					estado = tabla[posEstado][posEntrada];
					pila.push(entrada);
					pila.push(estado);
					concuerda = true;
					accionTxt = "Desp. " + entrada + " a " + estado;
					System.out.println("Resultado de la tabla (acción): " + accionTxt);
					renglonesSintactico.add(accionTxt);
					modeloSintactico.addRow(renglonesSintactico);
				}
				else if (accion.charAt(0) == 'P')
				{
					produccionAux = producciones[Integer.parseInt(accion.substring(1))].split(" ");
					switch (accion)
					{
						case "P0":
							ban = false;
							accionTxt = "P0 -> Cadena Aceptada";
							System.out.println("Resultado de la tabla (acción): " + accionTxt);
							renglonesSintactico.add(accionTxt);
							modeloSintactico.addRow(renglonesSintactico);
							JOptionPane.showMessageDialog(null, "Cadena Aceptada");
							return;
						case "P12":
							produccionTxt += "vacía";
							accionTxt = tabla[posEstado][posEntrada] + ": " + Producciones(accion) + " -> " + produccionTxt;
							System.out.println("Resultado de la tabla (acción): " + accionTxt);
							break;
						case "P16":
							produccionTxt += "vacía";
							accionTxt = tabla[posEstado][posEntrada] + ": " + Producciones(accion) + " -> " + produccionTxt;
							System.out.println("Resultado de la tabla (acción): " + accionTxt);
							break;
						default:
							for (int i = 0; i < produccionAux.length; i++)
								produccionTxt += produccionAux[i];
							accionTxt = tabla[posEstado][posEntrada] + ": " + Producciones(accion) + " -> " + produccionTxt;
							System.out.println("Resultado de la tabla (acción): " + accionTxt);
							for (int i = 0; i < produccionAux.length; i++)
							{
								System.out.println("Pop: " + pila.pop());
								System.out.println("Pop: " + pila.pop());
							}

					}
					renglonesSintactico.add(accionTxt);
					estadoP = pila.peek();
					System.out.print("Peek: " + estadoP);
					pila.push(Producciones(tabla[posEstado][posEntrada]));
					for (int i = 0; i < estados.length; i++)
						if (estados[i].equals(estadoP))
						{
							posEstado = i;
							break;
						}
					System.out.println(" \tPosición en tabla: " + posEstado);
					entradaP = pila.peek();
					for (int i = 0; i < entradas.length; i++)
						if (entradas[i].equals(entradaP))
						{
							posEntrada = i;
							break;
						}
					System.out.println("Push: " + entradaP + " \tPosición en tabla: " + posEntrada);
					if (tabla[posEstado][posEntrada].charAt(0) == 'I')
					{
						System.out.println("Push: " + tabla[posEstado][posEntrada]);
						pila.push(tabla[posEstado][posEntrada]);
						estado = pila.peek();
					}
					modeloSintactico.addRow(renglonesSintactico);
				}
			}
		}
	}

	private void Errores(String estado, String entrada)
	{
		switch (estado)
		{
			case "I0":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada 
						+ " inesperado.\nSe quedó esperando un: \n\t-> id \n\t-> int \n\t-> float \n\t-> char";
				break;
			case "I1":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada 
						+ " inesperado.\nSe quedó esperando un: \n\t-> $ (terminador de cadena)";
				break;
			case "I2":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> id";
				break;
			case "I3":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> $ (terminador de cadena)";
				break;
			case "I4":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> id";
				break;
			case "I5":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> id";
				break;
			case "I6":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> id";
				break;
			case "I7":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> =";
				break;
			case "I8":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> , \n\t-> ;";
				break;
			case "I9":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> id \n\t-> ( \n\t-> num";
				break;
			case "I10":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> $ (terminador de cadena)";
				break;
			case "I11":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> id";
				break;
			case "I12":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> id \n\t-> int \n\t-> float \n\t-> char";
				break;
			case "I13":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ;";
				break;
			case "I14":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> + \n\t-> - \n\t-> )";
				break;
			case "I15":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> + \n\t-> - \n\t-> * \n\t-> / \n\t-> )";
				break;
			case "I16":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> + \n\t-> - \n\t-> * \n\t-> / \n\t-> )";
				break;
			case "I17":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> id \n\t-> ( \n\t-> num";
				break;
			case "I18":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> + \n\t-> - \n\t-> * \n\t-> / \n\t-> )";
				break;
			case "I19":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> , \n\t-> ;";
				break;
			case "I20":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> $ (terminador de cadena)";
				break;
			case "I21":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> $ (terminador de cadena)";
				break;
			case "I22":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> )";
				break;
			case "I23":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> id \n\t-> ( \n\t-> num";
				break;
			case "I24":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> id \n\t-> ( \n\t-> num";
				break;
			case "I25":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> + \n\t-> - \n\t-> )";
				break;
			case "I26":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> id \n\t-> ( \n\t-> num";
				break;
			case "I27":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> id \n\t-> ( \n\t-> num";
				break;
			case "I28":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> )";
				break;
			case "I29":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> $ (terminador de cadena)";
				break;
			case "I30":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> + \n\t-> - \n\t-> )";
				break;
			case "I31":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> + \n\t-> - \n\t-> * \n\t-> / \n\t-> )";
				break;
			case "I32":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> + \n\t-> - \n\t-> * \n\t-> / \n\t-> )";
				break;
			case "I33":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> + \n\t-> - \n\t-> * \n\t-> / \n\t-> )";
				break;
			case "I34":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> + \n\t-> - \n\t-> * \n\t-> / \n\t-> )";
				break;
			case "I35":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> )";
				break;
			case "I36":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> )";
				break;
			case "I37":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> + \n\t-> - \n\t-> )";
				break;
			case "I38":
				resultadoError = "Error sintáctico en la línea: " + numeroLinea + ".\nCaracter: " + entrada
						+ " inesperado.\nSe quedó esperando un: \n\t-> ; \n\t-> + \n\t-> - \n\t-> )";
		}
	}

	private void rellenarTabla()
	{
		tabla[0][0] = "I7";
		tabla[0][1] = "I4";
		tabla[0][2] = "I5";
		tabla[0][3] = "I6";
		tabla[0][15] = "I1";
		tabla[0][16] = "I2";
		tabla[0][18] = "I3";

		tabla[1][14] = "P0";

		tabla[2][0] = "I8";

		tabla[3][14] = "P2";

		tabla[4][0] = "P3";

		tabla[5][0] = "P4";

		tabla[6][0] = "P5";

		tabla[7][12] = "I9";

		tabla[8][4] = "I11";
		tabla[8][5] = "I12";
		tabla[8][17] = "I10";

		tabla[9][0] = "I16";
		tabla[9][10] = "I17";
		tabla[9][13] = "I18";
		tabla[9][19] = "I13";
		tabla[9][21] = "I14";
		tabla[9][23] = "I15";

		tabla[10][14] = "P1";

		tabla[11][0] = "I19";

		tabla[12][0] = "I7";
		tabla[12][1] = "I4";
		tabla[12][2] = "I5";
		tabla[12][3] = "I6";
		tabla[12][15] = "I20";
		tabla[12][16] = "I2";
		tabla[12][18] = "I3";

		tabla[13][5] = "I21";

		tabla[14][5] = "P12";
		tabla[14][6] = "I23";
		tabla[14][7] = "I24";
		tabla[14][11] = "P12";
		tabla[14][20] = "I22";

		tabla[15][5] = "P16";
		tabla[15][6] = "P16";
		tabla[15][7] = "P16";
		tabla[15][8] = "I26";
		tabla[15][9] = "I27";
		tabla[15][11] = "P16";
		tabla[15][22] = "I25";

		tabla[16][5] = "P17";
		tabla[16][6] = "P17";
		tabla[16][7] = "P17";
		tabla[16][8] = "P17";
		tabla[16][9] = "P17";
		tabla[16][11] = "P17";

		tabla[17][0] = "I16";
		tabla[17][10] = "I17";
		tabla[17][13] = "I18";
		tabla[17][19] = "I28";
		tabla[17][21] = "I14";
		tabla[17][23] = "I15";

		tabla[18][5] = "P19";
		tabla[18][6] = "P19";
		tabla[18][7] = "P19";
		tabla[18][8] = "P19";
		tabla[18][9] = "P19";
		tabla[18][11] = "P19";

		tabla[19][4] = "I11";
		tabla[19][5] = "I12";
		tabla[19][17] = "I29";

		tabla[20][14] = "P7";

		tabla[21][14] = "P8";

		tabla[22][5] = "P9";
		tabla[22][11] = "P9";

		tabla[23][0] = "I16";
		tabla[23][10] = "I17";
		tabla[23][13] = "I18";
		tabla[23][21] = "I30";
		tabla[23][23] = "I15";

		tabla[24][0] = "I16";
		tabla[24][10] = "I17";
		tabla[24][13] = "I18";
		tabla[24][21] = "I31";
		tabla[24][23] = "I15";

		tabla[25][5] = "P13";
		tabla[25][6] = "P13";
		tabla[25][7] = "P13";
		tabla[25][11] = "P13";

		tabla[26][0] = "I16";
		tabla[26][10] = "I17";
		tabla[26][13] = "I18";
		tabla[26][23] = "I32";

		tabla[27][0] = "I16";
		tabla[27][10] = "I17";
		tabla[27][13] = "I18";
		tabla[27][23] = "I33";

		tabla[28][11] = "I34";

		tabla[29][14] = "P6";

		tabla[30][5] = "P12";
		tabla[30][6] = "I23";
		tabla[30][7] = "I24";
		tabla[30][11] = "P12";
		tabla[30][20] = "I35";

		tabla[31][5] = "P12";
		tabla[31][6] = "I23";
		tabla[31][7] = "I24";
		tabla[31][11] = "P12";
		tabla[31][20] = "I35";

		tabla[32][5] = "P16";
		tabla[32][6] = "P16";
		tabla[32][7] = "P16";
		tabla[32][8] = "I26";
		tabla[32][9] = "I27";
		tabla[32][11] = "P16";
		tabla[32][22] = "I37";

		tabla[33][5] = "P16";
		tabla[33][6] = "P16";
		tabla[33][7] = "P16";
		tabla[33][8] = "I26";
		tabla[33][9] = "I27";
		tabla[33][11] = "P16";
		tabla[33][22] = "I38";

		tabla[34][5] = "P18";
		tabla[34][6] = "P18";
		tabla[34][7] = "P18";
		tabla[34][8] = "P18";
		tabla[34][9] = "P18";
		tabla[34][11] = "P18";

		tabla[35][5] = "P10";
		tabla[35][11] = "P10";

		tabla[36][5] = "P11";
		tabla[36][11] = "P11";

		tabla[37][5] = "P14";
		tabla[37][6] = "P14";
		tabla[37][7] = "P14";
		tabla[37][11] = "P14";

		tabla[38][5] = "P15";
		tabla[38][6] = "P15";
		tabla[38][7] = "P15";
		tabla[38][11] = "P15";
	}

	private void rellenarProducciones()
	{
		producciones[0] = "P";
		producciones[1] = "TIPO id V";
		producciones[2] = "A";
		producciones[3] = "int";
		producciones[4] = "float";
		producciones[5] = "char";
		producciones[6] = ", id V";
		producciones[7] = "; P";
		producciones[8] = "id = EXP ;";
		producciones[9] = "TERM E";
		producciones[10] = "+ TERM E";
		producciones[11] = "- TERM E";
		producciones[12] = "";
		producciones[13] = "F T";
		producciones[14] = "* F T";
		producciones[15] = "/ F T";
		producciones[16] = "";
		producciones[17] = "id";
		producciones[18] = "( EXP )";
		producciones[19] = "num";
	}

	private String Producciones(String produccion)
	{
		switch (produccion)
		{
			case "P0":
				return "P'";
			case "P1":
				return "P";
			case "P2":
				return "P";
			case "P3":
				return "TIPO";
			case "P4":
				return "TIPO";
			case "P5":
				return "TIPO";
			case "P6":
				return "V";
			case "P7":
				return "V";
			case "P8":
				return "A";
			case "P9":
				return "EXP";
			case "P10":
				return "E";
			case "P11":
				return "E";
			case "P12":
				return "E";
			case "P13":
				return "TERM";
			case "P14":
				return "T";
			case "P15":
				return "T";
			case "P16":
				return "T";
			case "P17":
				return "F";
			case "P18":
				return "F";
			case "P19":
				return "F";
		}
		return "";
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == bAbrir || e.getSource() == itemAbrir)
		{
			this.Abrir();
		}
		else if (e.getSource() == bGuardarComo || e.getSource() == itemGuardarComo)
		{
			this.GuardarComo();
		}
		else if (e.getSource() == bNuevo || e.getSource() == itemNuevo)
		{
			this.Nuevo();
		}
		else if (e.getSource() == bGuardar || e.getSource() == itemGuardar)
		{
			this.Guardar();
		}
		else if (e.getSource() == bCerrar || e.getSource() == itemCerrar)
		{
			this.Cerrar();
		}
		else if (e.getSource() == bAnalizar)
		{
			ban = true;
			areaLexico.setEnabled(true);
			areaErrores.setEnabled(true);
			modeloSintactico.setRowCount(0); // limpia la tabla sintáctica
			modeloSimbolos.setRowCount(0); // limpia la tabla de símbolos
			resultadoLex = resultadoError = ""; // limpia los resultados de errores
			this.analisisLexico(); // manda llamar el análisis léxico
			areaErrores.setText(resultadoError); // establece la pantalla de errores
		}
	}
        
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        etTitulo = new javax.swing.JLabel();
        bNuevo = new javax.swing.JButton();
        bAbrir = new javax.swing.JButton();
        bGuardar = new javax.swing.JButton();
        bGuardarComo = new javax.swing.JButton();
        bCerrar = new javax.swing.JButton();
        jScrollPaneFuente = new javax.swing.JScrollPane();
        areaFuente = new javax.swing.JTextArea();
        jTabbedPaneAnalizado = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPaneLexico = new javax.swing.JScrollPane();
        areaLexico = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jScrollPaneSintactico = new javax.swing.JScrollPane();
        tablaSintactico = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jScrollPaneSemantico = new javax.swing.JScrollPane();
        areaSemantico = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jScrollPaneErrores = new javax.swing.JScrollPane();
        areaErrores = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jScrollPaneTablaSimbolos = new javax.swing.JScrollPane();
        tablaSimbolos = new javax.swing.JTable();
        etFuente = new javax.swing.JLabel();
        bAnalizar = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuArchivo = new javax.swing.JMenu();
        itemAbrir = new javax.swing.JMenuItem();
        itemNuevo = new javax.swing.JMenuItem();
        menuGuardar = new javax.swing.JMenu();
        itemGuardar = new javax.swing.JMenuItem();
        itemGuardarComo = new javax.swing.JMenuItem();
        menuCerrar = new javax.swing.JMenu();
        itemCerrar = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ANALIZADOR");

        etTitulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        etTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        etTitulo.setText("ANALIZADOR SINTÁCTICO");

        bNuevo.setBackground(new java.awt.Color(153, 255, 153));
        bNuevo.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        bNuevo.setText("Nuevo");

        bAbrir.setBackground(new java.awt.Color(153, 255, 153));
        bAbrir.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        bAbrir.setText("Abrir");

        bGuardar.setBackground(new java.awt.Color(153, 255, 153));
        bGuardar.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        bGuardar.setText("Guardar");

        bGuardarComo.setBackground(new java.awt.Color(153, 255, 153));
        bGuardarComo.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        bGuardarComo.setText("Guardar Como");

        bCerrar.setBackground(new java.awt.Color(153, 255, 153));
        bCerrar.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        bCerrar.setText("Cerrar");

        areaFuente.setColumns(20);
        areaFuente.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        areaFuente.setRows(5);
        jScrollPaneFuente.setViewportView(areaFuente);

        jTabbedPaneAnalizado.setFocusable(false);
        jTabbedPaneAnalizado.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        areaLexico.setColumns(20);
        areaLexico.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        areaLexico.setRows(5);
        jScrollPaneLexico.setViewportView(areaLexico);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneLexico, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneLexico, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
        );

        jTabbedPaneAnalizado.addTab("Léxico", jPanel2);

        tablaSintactico.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tablaSintactico.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPaneSintactico.setViewportView(tablaSintactico);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneSintactico, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneSintactico, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
        );

        jTabbedPaneAnalizado.addTab("Sintáctico", jPanel3);

        areaSemantico.setColumns(20);
        areaSemantico.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        areaSemantico.setRows(5);
        jScrollPaneSemantico.setViewportView(areaSemantico);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneSemantico, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneSemantico, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
        );

        jTabbedPaneAnalizado.addTab("Semántico", jPanel4);

        areaErrores.setColumns(20);
        areaErrores.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        areaErrores.setRows(5);
        jScrollPaneErrores.setViewportView(areaErrores);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneErrores, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneErrores, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
        );

        jTabbedPaneAnalizado.addTab("Errores", jPanel5);

        tablaSimbolos.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tablaSimbolos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPaneTablaSimbolos.setViewportView(tablaSimbolos);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPaneTablaSimbolos, javax.swing.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneTablaSimbolos, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
        );

        jTabbedPaneAnalizado.addTab("Tabla de Símbolos", jPanel1);

        etFuente.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        etFuente.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        etFuente.setText("Código Fuente");

        bAnalizar.setBackground(new java.awt.Color(153, 255, 153));
        bAnalizar.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        bAnalizar.setText("Analizar");

        menuArchivo.setText("Archivo");

        itemAbrir.setText("Abrir");
        menuArchivo.add(itemAbrir);

        itemNuevo.setText("Nuevo");
        menuArchivo.add(itemNuevo);

        jMenuBar1.add(menuArchivo);

        menuGuardar.setText("Guardar");

        itemGuardar.setText("Guardar");
        menuGuardar.add(itemGuardar);

        itemGuardarComo.setText("Guardar Como");
        menuGuardar.add(itemGuardarComo);

        jMenuBar1.add(menuGuardar);

        menuCerrar.setText("Cerrar");

        itemCerrar.setText("Cerrar");
        menuCerrar.add(itemCerrar);

        jMenuBar1.add(menuCerrar);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(etTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(bNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bAbrir, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(bGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bGuardarComo, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jScrollPaneFuente)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(etFuente, javax.swing.GroupLayout.PREFERRED_SIZE, 487, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addComponent(jTabbedPaneAnalizado, javax.swing.GroupLayout.PREFERRED_SIZE, 620, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(395, 395, 395)
                .addComponent(bAnalizar, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(etTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bNuevo)
                    .addComponent(bAbrir)
                    .addComponent(bGuardar)
                    .addComponent(bGuardarComo)
                    .addComponent(bCerrar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(etFuente, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPaneFuente))
                    .addComponent(jTabbedPaneAnalizado))
                .addGap(18, 18, 18)
                .addComponent(bAnalizar, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 15, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Analizador().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea areaErrores;
    private javax.swing.JTextArea areaFuente;
    private javax.swing.JTextArea areaLexico;
    private javax.swing.JTextArea areaSemantico;
    private javax.swing.JButton bAbrir;
    private javax.swing.JButton bAnalizar;
    private javax.swing.JButton bCerrar;
    private javax.swing.JButton bGuardar;
    private javax.swing.JButton bGuardarComo;
    private javax.swing.JButton bNuevo;
    private javax.swing.JLabel etFuente;
    private javax.swing.JLabel etTitulo;
    private javax.swing.JMenuItem itemAbrir;
    private javax.swing.JMenuItem itemCerrar;
    private javax.swing.JMenuItem itemGuardar;
    private javax.swing.JMenuItem itemGuardarComo;
    private javax.swing.JMenuItem itemNuevo;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPaneErrores;
    private javax.swing.JScrollPane jScrollPaneFuente;
    private javax.swing.JScrollPane jScrollPaneLexico;
    private javax.swing.JScrollPane jScrollPaneSemantico;
    private javax.swing.JScrollPane jScrollPaneSintactico;
    private javax.swing.JScrollPane jScrollPaneTablaSimbolos;
    private javax.swing.JTabbedPane jTabbedPaneAnalizado;
    private javax.swing.JMenu menuArchivo;
    private javax.swing.JMenu menuCerrar;
    private javax.swing.JMenu menuGuardar;
    private javax.swing.JTable tablaSimbolos;
    private javax.swing.JTable tablaSintactico;
    // End of variables declaration//GEN-END:variables
}

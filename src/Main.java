// 	SENG 300 Group 14 - Project #1
//	Pat Sluth : 30032750
//	Preston : XXXXXXX
//	Aaron Hornby: 10176084

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class Main {
	
	private static int declarationCount = 0;
	private static int referenceCount = 0;
	private static String typeName;
	private static String packageName; 

	public static void main(String[] args) {	
		
		String directoryPath;
		
		if (args.length == 2) {
			directoryPath = args[0];
			typeName = args[1];
		}
		else {
			System.out.println("Usage: java Main <directoryPath> <typeName>");
			return; 
		}
		
		if (parse(directoryPath, typeName)) 
			System.out.println(typeName + ". Declarations found: " + declarationCount + "; references found: " + referenceCount + "."); 

	}
	
	private static boolean parse(String directoryPath, String filteredTypeName) { 
		
		File directoryFile = new File(directoryPath);
		if (!directoryFile.isDirectory()) {
			System.out.println("Invalid directory.");
			return false;
		}
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
	
		for (File file : directoryFile.listFiles()) {
			
			if (file.isDirectory())
				continue; // prevent trying to parse a sub-directory
			
			String filename = file.getName();
			String[] parts = filename.split("\\.");
			String extension = parts[parts.length - 1]; 
			
			if (!extension.toLowerCase().equals("java")) 
				continue; // prevent trying to parse a non .java file
			
			try {	
				String source = readFile(file);
				
				//System.out.println("Reading file: " + file.getPath());
	
				parser.setSource(source.toCharArray());
				
				Map options = JavaCore.getOptions();
				JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options); // update compiler version to 1.5 to recognize enum declarations
				parser.setCompilerOptions(options);
	
				CompilationUnit rootNode = (CompilationUnit) parser.createAST(null);
				
				packageName = ""; // reset the package name before parsing next file
				
				rootNode.accept(new DeclarationVisitor()); 
				
				rootNode.accept(new FieldDeclarationVisitor(new FieldDeclarationVisitor.Listener() {
					@Override
					public void didVisitNodeOfType(Type type) 
					{
						String typeName = type.toString();
						
						if (filteredTypeName == null || typeName.equals(filteredTypeName)) {
							//System.out.println("\t" + typeName);
							referenceCount++; 
						}
					}
				}));
				
			} catch (FileNotFoundException e) {
				System.out.println("Failed to read file: " + file.getPath());
				return false; 
			}
		}
		
		return true; 
	}	

	private static String readFile(File file) throws FileNotFoundException
	{
	    Scanner scanner = new Scanner(file);
	    String source = "";
	
	    while (scanner.hasNextLine()) {
	    	source += scanner.nextLine() + "\n";
	    }
	    
	    scanner.close();
	
	    return source;
	}
	
	static class DeclarationVisitor extends ASTVisitor{
		
		public boolean visit(PackageDeclaration node) {
			packageName = node.getName().getFullyQualifiedName(); 
			return true; 
		}
		
		public boolean visit(AnnotationTypeDeclaration node) {
			String name = node.getName().getFullyQualifiedName();
			String fqn; // fully qualified name
			if (!packageName.equals(""))
				fqn = packageName + "." + name;
			else
				fqn = name; 
			if (!fqn.equals(typeName))
				return true;
			declarationCount++; 
			return true; 
		}
		
		
		public boolean visit(EnumDeclaration node) {
			String name = node.getName().getFullyQualifiedName();
			String fqn; // fully qualified name
			if (!packageName.equals(""))
				fqn = packageName + "." + name;
			else
				fqn = name; 
			if (!fqn.equals(typeName))
				return true; 
			declarationCount++; 
			return true; 
		}
		
		public boolean visit(TypeDeclaration node) {
			String name = node.getName().getFullyQualifiedName();
			String fqn; // fully qualified name
			if (!packageName.equals(""))
				fqn = packageName + "." + name;
			else
				fqn = name; 
			if (!fqn.equals(typeName))
				return true; 
			declarationCount++; 
			return true; 
		}
		
	}

}



class FieldDeclarationVisitor extends ASTVisitor
{	
	interface Listener
	{
		void didVisitNodeOfType(Type type);
	}
	
	private FieldDeclarationVisitor.Listener listener = null;
	
	
	public FieldDeclarationVisitor(FieldDeclarationVisitor.Listener listener) 
	{
		this.listener = listener;
	}
	
	@Override
	public boolean visit(FieldDeclaration node) 
	{
		if (this.listener != null) {
			this.listener.didVisitNodeOfType(node.getType());
		}
		
		return super.visit(node);
	}
}

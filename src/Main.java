// 	SENG 300 Group 14 - Project #1
//	Pat Sluth: 30032750
//	Preston Haffey: 10043064
//	Aaron Hornby: 10176084

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList; 

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class Main {
	
	public static int declarationCount = 0;
	public static int referenceCount = 0;
	private static String typeName;
	private static String packageName;
	private static String importName;
	private static List<String> decFQNS = new ArrayList<String>(); 

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
	
	public static boolean parse(String directoryPath, String filteredTypeName) { 
		
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
	
				parser.setSource(source.toCharArray());
				
				Map options = JavaCore.getOptions();
				JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options); // update compiler version to 1.5 to recognize enum declarations
				parser.setCompilerOptions(options);
	
				CompilationUnit rootNode = (CompilationUnit) parser.createAST(null);
				
				packageName = ""; // reset the package name before parsing next file
				
				decFQNS.clear(); // reset list of declarations found in the file
				
				importName = ""; // reset import name before parsing next file
				
				rootNode.accept(new DeclarationVisitor()); 
				
				rootNode.accept(new FieldDeclarationVisitor());
				
			} catch (FileNotFoundException e) {
				System.out.println("Failed to read file: " + file.getPath());
				return false; 
			}
		}
		
		return true; 
	}	

	public static String readFile(File file) throws FileNotFoundException
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
			packageName = node.getName().toString();
			return true; 
		}
		
		public boolean visit(ImportDeclaration node) {
			String name = node.getName().toString();
			if (name.equals(typeName)) {
				String[] parts = name.split("\\.");
				importName = parts[parts.length-1]; 
			}
			return true;
		}
		
		public boolean visit(AnnotationTypeDeclaration node) {
			
			String simpleName = node.getName().toString();
			String fqn = getFQN(node, simpleName); 
			
			decFQNS.add(fqn);
			
			if (!fqn.equals(typeName))
				return true; 
			declarationCount++; // only increment counter if the fqn equals the typeName
			return true;
		}
		
		
		public boolean visit(EnumDeclaration node) {
			
			String simpleName = node.getName().toString();
			String fqn = getFQN(node, simpleName); 
			
			decFQNS.add(fqn);
			
			if (!fqn.equals(typeName))
				return true; 
			declarationCount++; // only increment counter if the fqn equals the typeName
			return true; 
		}
		
		public boolean visit(TypeDeclaration node) {
			
			String simpleName = node.getName().toString();
			String fqn = getFQN(node, simpleName); 
			
			decFQNS.add(fqn); 
			
			if (!fqn.equals(typeName))
				return true; 
			declarationCount++; // only increment counter if the fqn equals the typeName
			return true; 
		}
		
		private String getFQN(ASTNode node, String simpleName) {
			
			String fqn = simpleName; // init fqn as inner class/interface simple name
			
			ASTNode parent = node.getParent(); // get either an outer class/interface/enum/annotation or the comp. unit
			int parentType = parent.getNodeType(); 
			while (true) { // keep appending the outer class/interface/enum/annotation names to fqn
				String parentName; 
				if (parentType == ASTNode.ANNOTATION_TYPE_DECLARATION)
					parentName = ((AnnotationTypeDeclaration) parent).getName().toString();
				else if (parentType == ASTNode.ENUM_DECLARATION)
					parentName = ((EnumDeclaration) parent).getName().toString();
				else if (parentType == ASTNode.TYPE_DECLARATION)
					parentName = ((TypeDeclaration) parent).getName().toString();
				else
					break; 
				fqn = parentName + "." + fqn;
				parent = parent.getParent(); // move up one parent level
				parentType = parent.getNodeType(); // get type of new parent
			}

			if (!packageName.equals(""))
				fqn = packageName + "." + fqn; // append the package name to front if one was explicitly declared
			
			return fqn; 
		}
		
	}
	
	static class FieldDeclarationVisitor extends ASTVisitor
	{	
		
		public boolean visit(FieldDeclaration node) 
		{
			
			String refName = node.getType().toString();
			
			for(int i = 0; i < decFQNS.size(); i++) {
				String fqn = decFQNS.get(i);
				String[] parts = fqn.split("\\.");
				String className = parts[parts.length-1]; 
				if (className.equals(refName)) {
					refName = fqn; 
					break; 
				}
			}
			
			if (refName.equals(typeName) || (!importName.equals("") && refName.equals(importName))) {
				referenceCount++;
				// now check for any constructor:
				List fragments = node.fragments();
				if (fragments.size() > 0) {
					if (fragments.get(0).toString().contains("new"))
						referenceCount++; 
				}
			} 
			
			return true;
		}
	}

}

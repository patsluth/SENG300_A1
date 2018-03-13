// 	SENG 300 Group 14 - Project #1
//	Pat Sluth : 30032750
//	Preston : 10043064
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
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class Main {
	
	private static int declarationCount = 0;
	private static int referenceCount = 0;
	private static String typeName;
	private static String packageName;
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
				
				decFQNS.clear(); // reset
				
				rootNode.accept(new DeclarationVisitor()); 
				
				System.out.println(decFQNS.size());
				
				rootNode.accept(new FieldDeclarationVisitor());
				/*
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
				*/
				
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
			packageName = node.getName().toString();
			return true; 
		}
		
		public boolean visit(AnnotationTypeDeclaration node) {
			
			String simpleName = node.getName().toString();
			String fqn = getFQN(node, simpleName); 
			
			
			if (!fqn.equals(typeName))
				return true; 
			declarationCount++; // only increment counter if the fqn equals the typeName
			return true;
		}
		
		
		public boolean visit(EnumDeclaration node) {
			
			String simpleName = node.getName().toString();
			String fqn = getFQN(node, simpleName); 
			
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
		/*
		interface Listener
		{
			void didVisitNodeOfType(Type type);
		}
		
		private FieldDeclarationVisitor.Listener listener = null;
		
		
		public FieldDeclarationVisitor(FieldDeclarationVisitor.Listener listener) 
		{
			this.listener = listener;
		}
		*/
		
		@Override
		public boolean visit(FieldDeclaration node) 
		{
			/*
			String typeName = node.getType().toString();
			boolean attachedConstructor = false; 
			List fragments = node.fragments();
			System.out.println(node.fragments().size());
			for (Object o: fragments) {
				System.out.println(o.toString());
			}
			*/
			
			String typeName = node.getType().toString();
			System.out.println("refname:     " +typeName);
			
			for(int i = 0; i < decFQNS.size(); i++) {
				String fqn = decFQNS.get(i);
				//System.out.println(fqn);
				String[] parts = fqn.split("\\.");
				//System.out.println("end: " +parts[parts.length-1]);
				if (parts[parts.length-1].equals(typeName)){
					typeName = fqn; 
					break; 
				}
			}
			
			System.out.println("fqn: " +typeName);
			if (typeName.equals(Main.typeName))
				referenceCount++; 
			
			
//			if (fragments.size() > 0) {
//				if (fragments.get(0).toString().contains(typeName)) {
//					
//					System.out.println(node.fragments().get(0));
//					attachedConstructor = true;
//					System.out.println("CONSTRUCTOR FOUND!");
//				}
//			}
			/*
			if (this.listener != null) {
				this.listener.didVisitNodeOfType(node.getType());
			}
			*/
			
			return super.visit(node);
		}
	}

}

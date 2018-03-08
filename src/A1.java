// 	SENG 300 Group A1
//	Pat Sluth : 30032750
//	Preston : XXXXXXX
//	Ashton : XXXXXXX

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Type;





public class A1
{
	public void parse(String directoryPath, String filteredTypeName, AtomicInteger outDeclarationCount, AtomicInteger outReferenceCount)
	{
		outDeclarationCount.set(0);
		outReferenceCount.set(0);
		
		
		
		File directoryFile = new File(directoryPath);
		assert(directoryFile.isDirectory());
		
		
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		
		
		for (File file : directoryFile.listFiles()) {
			try {
				String source = this.readFile(file);
				System.out.println("Read file " + file.getPath());

				parser.setSource(source.toCharArray());

				CompilationUnit compilationUnit = (CompilationUnit)parser.createAST(null);
				compilationUnit.accept(new FieldDeclarationVisitor(new FieldDeclarationVisitor.Listener() {
					@Override
					public void didVisitNodeOfType(Type type) 
					{
						String typeName = type.toString();
						
						if (filteredTypeName == null || typeName.equals(filteredTypeName)) {
							System.out.println("\t" + typeName);
							outDeclarationCount.set(outDeclarationCount.get() + 1);
						}
					}
				}));
			} catch (FileNotFoundException e) {
				System.out.println("Failed to reads file " + file.getPath());
			}
		}
	}
	
	private String readFile(File file) throws FileNotFoundException
    {
        Scanner scanner = new Scanner(file);
        String string = "";

        while (scanner.hasNextLine()) {
        		string += scanner.nextLine();
        }
        
        scanner.close();

        return string;
    }
	
	
	
	
	
	public static void main(String[] args) 
	{
		String directoryPath = "/Volumes/Malish/Development/eclipse_workspace/CPSC441_A1/src";
		String typeName = null;
		typeName = "int";
//		typeName = "String[]";
		
		if (args.length == 2) {
			directoryPath = args[0];
			typeName = args[1];
		}
		
		A1 a1 = new A1();
		AtomicInteger declarationCount = new AtomicInteger(0);
		AtomicInteger referenceCount = new AtomicInteger(0);
		a1.parse(directoryPath, typeName, declarationCount, referenceCount);
		System.out.printf("declarationCount = %d\n", declarationCount.get());
		System.out.printf("referenceCount = %d\n", referenceCount.get());
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





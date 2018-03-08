// 	Pat Sluth
// 	SENG 300
//	30032750

import org.apache.commons.lang.ObjectUtils.Null;
import org.eclipse.core.internal.localstore.Bucket.Visitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import org.junit.Test;





public class A1 
{
	public A1(String directoryPath, String typeName)
	{
		File directoryFile = new File(directoryPath);
		assert(directoryFile.isDirectory());
		
		for (File file : directoryFile.listFiles()) {
//			System.out.println(file.getPath());
			try {
				String source = this.readFile(file);
				System.out.println("Read file " + file.getPath());
				
				
				
				ASTParser parser = ASTParser.newParser(AST.JLS3);
				parser.setKind(ASTParser.K_COMPILATION_UNIT);
				parser.setSource(source.toCharArray());

				CompilationUnit compilationUnit = (CompilationUnit)parser.createAST(null);
				compilationUnit.accept(new FieldVisitor(typeName));
				
				
				
			} catch (FileNotFoundException e) {
				System.out.println("Failed to reads file " + file.getPath());
			}
		}
		
//		try {
//			String source = this.stringFromFile(filePath);
//			//System.out.println(source);
//			
//			ASTParser parser = ASTParser.newParser(AST.JLS3);
//
//			parser.setKind(ASTParser.K_COMPILATION_UNIT);
//			parser.setSource(source.toCharArray());
//
//			CompilationUnit compilationUnit = (CompilationUnit)parser.createAST(null);
//			compilationUnit.accept(new ASTVisitor()
//			{
//				@Override
//				public boolean visit(FieldDeclaration node) 
//				{
//
////					Type t = node.getType();
//					System.out.println(node.getType());
//
//					return super.visit(node);
//				}
//			}); 
//			
//		} catch (FileNotFoundException e) {
//			System.out.printf("Failed to read file at %s\n", filePath);
//		}
	}
	
	private class FieldVisitor extends ASTVisitor
	{
		private String filteredTypeName = null;
		
		public FieldVisitor(String filteredTypeName) 
		{
			this.filteredTypeName = filteredTypeName;
		}
		
		@Override
		public boolean visit(FieldDeclaration node) 
		{
			if (node.getType().toString().equals(this.filteredTypeName)) {
				System.out.println(node.getType());
			}
			return super.visit(node);
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
		// TODO: get from args
		String directoryPath = "/Volumes/Malish/Development/eclipse_workspace/CPSC441_A1/src";
		String typeName = "String[]";
		
		new A1(directoryPath, typeName);
	}
}





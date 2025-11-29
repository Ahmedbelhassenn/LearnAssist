export interface Participant{
    firstName: string;
    lastName: string;
    phone: string;
    email: string;
    gender?: string; 
    dateOfBirth?: string; 
    profilePhoto?: string; 
    city: string;
    status?: string; // Optionnel
    bio?: string; // Optionnel
    educationLevel?: string; // Optionnel
    password: string;
    confirmPassword: string,
    profilePhotoUrl:string    
}
export interface Formation{
    title: string,
    description: string,
    imageUrl: string,
    price: string,
    formationDuration: string,
    formationLevel: string,
    videoUrl: string,
    /** Object URL of the video file downloaded from the backend (played with <video>). */
    videoBlobUrl?: string,
    formationStatus: string,
    contents: string[],
    instructorName: string,
    instructorProfilePhoto: string,
    instructorBio: string,
    videoFileName: string
    
}
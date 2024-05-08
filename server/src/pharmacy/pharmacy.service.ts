import { Injectable, OnModuleInit } from '@nestjs/common';
import {
  GPSLocation,
  MapChunk,
  Medicine,
  Pharmacy,
  Stock,
} from '@prisma/client';
import { PrismaService } from 'src/core/prisma.service';
import { CreatePharmacyDto } from './dtos/create-pharmacy.dto';
import { CreateStockDto } from './dtos/create-stock.dto';

const LAT_MAX = 38.81;
const LAT_MIN = 38.69;
const LNG_MAX = -9.05;
const LNG_MIN = -9.3;

const RANGE = 0.01;

@Injectable()
export class PharmacyService implements OnModuleInit {
  constructor(private prismaService: PrismaService) {}

  async onModuleInit() {
    await this.prismaService.pharmacy.deleteMany();
    await this.prismaService.mapChunk.deleteMany();

    for (let i = 1; i <= 261; i++) {
      const pharmacy = {
        name: `Pharmacy${i}`,
        location: {
          lat: parseFloat(
            (Math.random() * (LAT_MAX - LAT_MIN) + LAT_MIN).toFixed(6),
          ), // Random latitude between 100 and -200
          lng: parseFloat(
            (Math.random() * (LNG_MAX - LNG_MIN) + LNG_MIN).toFixed(6),
          ), // Random longitude between 100 and -200
        },
      };

      await this.createPharmacy(pharmacy);
    }

    this.getMapChunks(38.72, -9.1);
  }

  private precisionRound(n: number, step: number) {
    return Math.round((n + Number.EPSILON) * step) / step;
  }

  async getMapChunks(lat: number, lng: number) {
    // Find chunks

    const possibleChunks: GPSLocation[] = [
      {
        lat,
        lng,
      },
      {
        lat: lat - RANGE,
        lng: lng,
      },
      {
        lat: lat + RANGE,
        lng: lng,
      },
      {
        lat: lat,
        lng: lng - RANGE,
      },
      {
        lat: lat,
        lng: lng + RANGE,
      },
    ];

    const chunks = possibleChunks.map(({ lat, lng }) =>
      Buffer.from(`${lat}${lng}`).toString('base64'),
    );

    return (
      await this.prismaService.mapChunk.findMany({
        where: {
          chunkId: {
            in: chunks,
          },
        },
        include: {
          pharmacies: {
            select: {
              id: true,
              name: true,
              location: true,
            },
          },
        },
      })
    ).filter((chunk) => chunk.pharmacies.length > 0);
  }

  async getpharmacies(pageNumber: number = 1): Promise<Pharmacy[]> {
    const skip = (pageNumber - 1) * 10;
    const pharmacies = await this.prismaService.pharmacy.findMany({
      skip: skip,
      take: 10,
    });
    return pharmacies;
  }

  async getAllMedicinesInPharmacy(
    pharmacyId: string,
    skip: number,
  ): Promise<Medicine[]> {
    return await this.prismaService.pharmacy
      .findUnique({
        where: { id: pharmacyId },
      })
      .medicines({
        skip: skip,
        take: 10,
      });
  }

  async getPharmacyInfo(pharmacyId: string): Promise<PharmacyInfo> {
    const pharmacy = await this.prismaService.pharmacy.findUnique({
      where: { id: pharmacyId },
      select: {
        name: true,
        location: true,
        picture: true,
      },
    });

    if (!pharmacy) {
      throw new Error('Pharmacy not found');
    }

    return {
      name: pharmacy.name,
      location: pharmacy.location,
      picture: pharmacy.picture,
    };
  }

  async createPharmacy(dto: CreatePharmacyDto): Promise<Pharmacy> {
    const roundedLat = this.precisionRound(dto.location.lat, 50);
    const roundedLng = this.precisionRound(dto.location.lng, 50);
    const chunkLocation = `${roundedLat}${roundedLng}`;
    const chunkId = Buffer.from(chunkLocation).toString('base64');

    console.log(`Pharmacy chunk: ${chunkId} / Coords: ${chunkLocation}`);

    return await this.prismaService.pharmacy.create({
      data: {
        name: dto.name,
        location: dto.location,
        picture: '',
        rating: '',
        flagCount: 0,
        mapChunk: {
          connectOrCreate: {
            where: {
              chunkId: chunkId,
            },
            create: {
              chunkId: chunkId,
              location: {
                lat: roundedLat,
                lng: roundedLng,
              },
            },
          },
        },
      },
    });
  }

  async addMedicine(dto: CreateStockDto): Promise<Stock> {
    return await this.prismaService.stock.create({
      data: {
        medicineId: dto.medicineID,
        quantity: dto.quantity,
        pharmacyId: dto.pharmacyId,
      },
    });
  }

  async buyMedicine(pharmacyId: string, medicineId: string): Promise<Stock> {
    try {
      // Find the stock item based on pharmacyId and medicineId
      const existingStockItem = await this.prismaService.stock.findFirst({
        where: {
          pharmacyId: pharmacyId,
          medicineId: medicineId,
        },
      });

      // If the stock item exists, update its quantity
      if (existingStockItem) {
        const updatedStockItem = await this.prismaService.stock.update({
          where: {
            id: existingStockItem.id,
          },
          data: {
            quantity: existingStockItem.quantity - 1,
          },
        });
        console.log(
          `Stock quantity updated for medicine ${medicineId} in pharmacy ${pharmacyId}.`,
        );
        return updatedStockItem;
      } else {
        console.log(
          `Stock item not found for medicine ${medicineId} in pharmacy ${pharmacyId}.`,
        );
        return null;
      }
    } catch (error) {
      console.error('Error updating stock quantity:', error);
      throw error;
    }
  }
}

interface PharmacyInfo {
  name: string;
  location: GPSLocation;
  picture: string;
}
